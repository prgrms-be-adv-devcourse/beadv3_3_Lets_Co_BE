"""
[GutJJeu RAG Controller] 무중단 자동 스케일링 및 관제 타워
이 스크립트는 RAG 시스템의 '두뇌' 역할을 합니다. 
주기적으로 MariaDB의 데이터 규모를 측정하여 L1(Flat) ~ L5(Milvus) 중 최적의 검색 엔진을 자동으로 선택하고,
무중단(Zero-Downtime)으로 백그라운드 동기화 및 서버 교체를 수행합니다.
또한 L6(Elasticsearch) 임베딩 서버의 생존을 감시하고 실시간 동기화 요청을 각 엔진으로 라우팅합니다.
"""

import pymysql
import yaml
import subprocess
import os
import sys
import time
import signal
import requests
from pathlib import Path
from datetime import datetime
from apscheduler.schedulers.background import BackgroundScheduler
from rich import print

# ==========================================
# ⚙️ [1] 전역 변수 및 설정 관리
# ==========================================

# 현재 실행 중인 서버 프로세스 관리 (무중단 전환 및 안전한 종료를 위해 유지)
current_app_proc = None  # L1~L5 중 현재 가동 중인 벡터 검색 API 서버 프로세스
current_l6_proc = None   # L6(Elasticsearch) 전용 임베딩 및 동기화 서버 프로세스
current_level = None     # 현재 가동 중인 시스템 레벨 (예: "L1_Flat")

# 마이크로서비스 간 통신 주소 설정
# L1~L5 검색 엔진은 8077 포트, L6 하이브리드 동기화 서버는 8078 포트를 전담합니다.
L6_URL = "http://localhost:8078" 
RAG_ENGINE_URL = "http://localhost:8077"

def load_config():
    """rag_config.yml 설정 파일을 읽어 딕셔너리로 반환합니다."""
    config_path = Path("rag_config.yml")
    if not config_path.exists():
        raise FileNotFoundError("rag_config.yml 파일을 찾을 수 없습니다. 루트 경로를 확인하세요.")
    with open(config_path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f)

# ==========================================
# 📊 [2] 데이터 규모 모니터링 및 자동 레벨(L1~L5) 결정
# ==========================================

def get_total_count(cfg):
    """
    MariaDB에 접속하여 검색 대상이 되는 핵심 테이블들의 유효 데이터(Del=0) 총합을 계산합니다.
    이 수치가 시스템이 L1을 쓸지, L5를 쓸지 결정하는 절대적인 기준이 됩니다.
    """
    db_cfg = cfg['database']
    try:
        conn = pymysql.connect(
            host=db_cfg['url'],
            user=db_cfg['username'],
            password=db_cfg['password'],
            database=db_cfg['name'],
            port=db_cfg['port'],
            charset=db_cfg['charset']
        )
        total = 0
        # config.yml에 정의된 테이블 목록을 순회하며 카운트 누적
        tables = db_cfg.get('tables', ['Products', 'Customer_Service', 'Customer_Service_Detail'])
        with conn.cursor() as cursor:
            for table in tables:
                cursor.execute(f"SELECT COUNT(*) FROM {table} WHERE Del = 0")
                total += cursor.fetchone()[0]
        conn.close()
        return total
    except Exception as e:
        print(f"[bold red]❌ [DB 에러] 데이터 카운트 측정 실패 (DB 연결을 확인하세요): {e}[/bold red]")
        return None

def determine_level(count, thresholds):
    """측정된 데이터 총량을 기반으로 config.yml의 임계값(Thresholds)과 비교하여 최적의 RAG 폴더를 매칭합니다."""
    if count < thresholds.get('L1_limit', 10000): return "L1_Flat"         # 소규모: 무손실 완전 탐색
    elif count < thresholds.get('L2_limit', 100000): return "L2_HNSW"      # 중규모: 그래프 기반 고속 탐색
    elif count < thresholds.get('L3_limit', 500000): return "L3_IVFPQ"     # 대규모: 벡터 압축 탐색
    elif count < thresholds.get('L4_limit', 1000000): return "L4_Qdrant"   # 초대규모: 디스크 기반 상용 벡터 DB
    else: return "L5_Milvus"                                               # 엔터프라이즈: 클러스터형 상용 벡터 DB

# ==========================================
# 🔄 [3] 실시간 동기화 트리거 라우팅
# ==========================================

def trigger_realtime_sync(source_id: str):
    """
    [실시간 동기화 라우터] 
    백엔드(Spring)에서 특정 상품/게시글이 추가/수정/삭제되었을 때 컨트롤러로 알림을 보내면,
    컨트롤러가 현재 켜져 있는 벡터 엔진(L1~L5)과 하이브리드 엔진(L6) 양쪽에 동기화 명령을 뿌려줍니다.
    """
    print(f"\n[bold yellow]🔄 [{datetime.now().strftime('%H:%M:%S')}] '{source_id}' 통합 실시간 동기화 트리거 수신됨[/bold yellow]")
    
    # 1. L6 서버 호출 (Elasticsearch 업데이트 요청)
    try:
        requests.post(f"{L6_URL}/sync-item/{source_id}", timeout=5)
        print(f" ├── [green]✅ [L6 ES] 외부 Elasticsearch 임베딩 업데이트 지시 완료[/green]")
    except Exception:
        print(f" ├── [red]❌ [L6 ES] 통신 실패 (L6 서버가 다운되었을 수 있습니다)[/red]")

    # 2. 로컬 벡터 RAG 엔진 호출 (FAISS / Qdrant / Milvus 업데이트 요청)
    try:
        requests.post(f"{RAG_ENGINE_URL}/sync-item/{source_id}", timeout=5)
        print(f" └── [green]✅ [Vector DB] 로컬 RAG 엔진 지식 갱신 지시 완료[/green]")
    except Exception:
        print(f" └── [red]❌ [Vector DB] 통신 실패 (RAG 엔진 서버 상태 확인 필요)[/red]")

# ==========================================
# 🚀 [4] 시스템 기동 및 무중단 배포(Zero-Downtime) 관리
# ==========================================

def start_system():
    """
    1. 데이터 규모를 파악하여 레벨을 결정합니다.
    2. L6 서버가 죽어있다면 살려냅니다.
    3. 기존 API 서버를 켜둔 상태로 새 레벨의 db_sync.py(인덱스 구축)를 백그라운드에서 실행합니다.
    4. 인덱싱이 완료되면 서버를 안전하게 교체(무중단 배포)합니다.
    """
    global current_app_proc, current_l6_proc, current_level
    
    cfg = load_config()
    count = get_total_count(cfg)
    if count is None: return

    # 현재 데이터 규모에 맞는 최적의 엔진 레벨 판단
    new_level = determine_level(count, cfg.get('thresholds', {}))
    
    print(f"\n[bold cyan]{'='*65}[/bold cyan]")
    print(f"📅 [관제 모니터링] 시스템 스캔 시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"📊 [데이터 규모] 누적 유효 데이터: [bold yellow]{count:,}[/bold yellow] 건")
    print(f"🛠️ [엔진 선택] 최적의 RAG 아키텍처: [bold green]{new_level}[/bold green] 가동 준비")
    
    # ----------------------------------------------------
    # 1. L6 서버 자가 치유 (Self-Healing)
    # ----------------------------------------------------
    if current_l6_proc is None or current_l6_proc.poll() is not None:
        print(f"🏥 [L6 자가치유] L6 서버가 중단된 것을 감지했습니다. 재기동을 시도합니다...")
        l6_path = Path("L6_Elasticsearch/app.py")
        if l6_path.exists():
            current_l6_proc = subprocess.Popen([sys.executable, "app.py"], cwd="./L6_Elasticsearch")
            print("🚀 [bold green]L6 하이브리드 동기화 서버 가동 완료 (Port: 8078)[/bold green]")
    
    # ----------------------------------------------------
    # 2. 벡터 데이터베이스 인덱스 동기화 (기존 서비스 무중단 유지)
    # ----------------------------------------------------
    print(f"🔄 [{new_level}] 인덱스 최신화 프로세스 시작 (현재 API 서비스 정상 응답 중)...")
    try:
        # 각 레벨 폴더 내부의 전용 db_sync.py 실행 (Blocking)
        subprocess.run([sys.executable, "db_sync.py"], cwd=f"./{new_level}", check=True)
    except subprocess.CalledProcessError as e:
        print(f"[bold red]❌ [{new_level}] DB 동기화 스크립트 실행 실패: {e}[/bold red]")
        print("[yellow]⚠️ 치명적 오류 방지를 위해 서버 교체를 취소하고 기존 서버를 유지합니다.[/yellow]")
        return 

    # ----------------------------------------------------
    # 3. API 서버 핫 리로드 및 프로세스 교체
    # ----------------------------------------------------
    if current_app_proc:
        # 데이터만 바뀌고 레벨(엔진 종류)은 같다면, 프로세스를 껐다 켤 필요 없이
        # 각 app.py 내부에 구현된 index_watcher가 변경된 파일을 감지하여 메모리만 핫 리로드함
        if current_level == new_level:
            print(f"✨ [{new_level}] 동일 레벨 감지: 무중단 핫 리로드(Hot-Reload)가 자동으로 수행됩니다.")
            print(f"[bold cyan]{'='*65}[/bold cyan]\n")
            return

        # 레벨이 변경되었다면(예: L1 -> L2), 기존 레벨의 프로세스를 완전히 끄고 새 프로세스를 올려야 함
        print(f"📈 [스케일 업] 레벨 변경 감지 ([yellow]{current_level}[/yellow] ➔ [green]{new_level}[/green]). 구형 서버를 안전하게 내립니다.")
        current_app_proc.terminate()
        current_app_proc.wait() # 포트 점유 해제를 위해 대기

    # 4. 새로운 API 서버 프로세스 가동
    print(f"📡 [{new_level}] 신규 메인 RAG API 서버 프로세스를 기동합니다 (Port: 8077)...")
    current_app_proc = subprocess.Popen([sys.executable, "app.py"], cwd=f"./{new_level}")
    current_level = new_level
    print(f"[bold cyan]{'='*65}[/bold cyan]\n")

def shutdown_handler(sig, frame):
    """
    [우아한 종료 (Graceful Shutdown)] 
    Ctrl+C나 kill 명령어가 들어오면 고아(Orphan) 프로세스가 남지 않도록 모든 자식 프로세스를 정리합니다.
    """
    global current_app_proc, current_l6_proc
    print("\n[bold red]🛑 종료 시그널 수신됨. GutJJeu RAG 관제 시스템을 안전하게 종료합니다...[/bold red]")
    if current_app_proc:
        current_app_proc.terminate()
        print(" ├── 검색 엔진(L1~L5) 종료 완료")
    if current_l6_proc:
        current_l6_proc.terminate()
        print(" ├── 하이브리드 엔진(L6) 종료 완료")
    print("[bold red]👋 모든 RAG 시스템이 성공적으로 종료되었습니다.[/bold red]")
    sys.exit(0)

# ==========================================
# ⏰ [5] 메인 루프 및 스케줄러 등록
# ==========================================

if __name__ == "__main__":
    # ⭐ 터미널 화면 자동 클리어 (Windows/Linux/Mac 모두 호환)
    os.system('cls' if os.name == 'nt' else 'clear')

    # OS 종료 시그널 캐치 등록
    signal.signal(signal.SIGINT, shutdown_handler)
    signal.signal(signal.SIGTERM, shutdown_handler)

    print("\n[bold cyan]🦅 GutJJeu AI RAG 관제 컨트롤러 가동을 시작합니다.[/bold cyan]")

    # 1. 서버 부팅 시 최초 1회 즉시 스캔 및 기동
    start_system()

    # 2. 정기 자동 스케일링 스케줄러 설정 (매일 자정 00:00:00에 데이터 볼륨 재측정)
    scheduler = BackgroundScheduler(job_defaults={'misfire_grace_time': 3600})
    scheduler.add_job(start_system, 'cron', hour=0, minute=0, second=0)
    scheduler.start()
    print("⏰ [스케줄러] 매일 자정 데이터 규모 기반 '오토 스케일링' 관제가 활성화되었습니다.")

    # 3. 메인 쓰레드는 종료되지 않도록 무한 루프 대기 (인터럽트 대기)
    try:
        while True:
            time.sleep(1)
    except (KeyboardInterrupt, SystemExit):
        scheduler.shutdown()