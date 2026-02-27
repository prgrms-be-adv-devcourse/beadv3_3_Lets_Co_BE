"""
[L6_Elasticsearch] 수동 벡터 임베딩 및 업서트 스크립트
MariaDB의 상품 데이터를 조회하여 텍스트로 가공한 뒤, AI 모델을 통해 벡터(Vector)로 변환합니다.
변환된 벡터 데이터를 Elasticsearch의 `_bulk` API를 사용하여 대량으로 업서트(Upsert)합니다.
초기 데이터 적재나 강제 전체 동기화가 필요할 때 독립적으로 실행하는 스크립트입니다.
"""

import pymysql
import yaml
import json
import requests
import time
from pathlib import Path
from sentence_transformers import SentenceTransformer
from rich import print

# ==========================================
# ⚙️ [1] 환경 설정 및 ES 접속 정보 로드
# ==========================================
CONFIG_PATH = Path("rag_config.yml")
# 스크립트 실행 위치에 따라 경로 유연하게 탐색
if not CONFIG_PATH.exists():
    CONFIG_PATH = Path("../rag_config.yml")

with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

# Elasticsearch 클러스터 접속 정보 및 인덱스 규격
ES_URL = "http://52.79.170.61:30200"
INDEX_NAME = "products-index"
VECTOR_FIELD = "product_vector"   # ES에 저장될 벡터 필드명
# 🎯 식별자를 Products_IDX로 설정하여 DB와의 일관성 유지
ID_FIELD = "Products_IDX"

# MariaDB 접속 정보 조립
DB_CONFIG = {
    "host": cfg['database']['url'],
    "user": cfg['database']['username'],
    "password": cfg['database']['password'],
    "database": cfg['database']['name'],
    "port": cfg['database']['port'],
    "charset": cfg['database']['charset'],
    "cursorclass": pymysql.cursors.DictCursor
}

# ==========================================
# 🚀 [2] 메인 데이터 처리 함수
# ==========================================
def fetch_and_embed():
    """DB 데이터 조회 -> AI 벡터 변환 -> ES 전송의 전체 파이프라인을 실행합니다."""
    print(f"\n[bold magenta]🚀 [L6_ES] Elasticsearch 수동 벡터 업데이트 스크립트 가동[/bold magenta]")
    print(f"📍 [cyan]대상 서버:[/cyan] {ES_URL} | [cyan]대상 인덱스:[/cyan] {INDEX_NAME}")
    
    # ----------------------------------------------------
    # 1. DB 데이터 조회 (상품 상세 정보, 카테고리, 옵션 등)
    # ----------------------------------------------------
    print(f"[bold blue]📡 데이터베이스(MariaDB)에서 활성 상품(SALE, ON_SALE) 정보를 조회 중...[/bold blue]")
    query = """
        SELECT 
            p.Products_IDX, p.Products_Code, p.Products_Name, p.Description, p.Price, p.Sale_Price, p.Status,
            c1.Category_Name, c2.Category_Name AS IP_Name,
            GROUP_CONCAT(po.Option_Name SEPARATOR ', ') AS Options
        FROM Products p
        LEFT JOIN Products_Category c1 ON p.Products_Category = c1.Category_IDX
        LEFT JOIN Products_Category c2 ON p.Products_IP = c2.Category_IDX
        LEFT JOIN Product_Option po ON p.Products_IDX = po.Products_IDX AND po.Del = 0
        WHERE p.Del = 0 AND p.Status IN ('SALE', 'ON_SALE')
        GROUP BY p.Products_IDX
    """
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        with conn.cursor() as cursor:
            cursor.execute(query)
            rows = cursor.fetchall()
        conn.close()
        print(f"✅ DB 조회 완료: 대상 상품 총 [bold green]{len(rows)}[/bold green]건 확인됨")
    except Exception as e:
        print(f"[bold red]❌ DB 연결 또는 쿼리 실행 실패: {e}[/bold red]")
        return

    if not rows:
        print("[yellow]⚠️ 처리할 활성 상품 데이터가 존재하지 않습니다. 스크립트를 종료합니다.[/yellow]")
        return
    
    # ----------------------------------------------------
    # 2. AI 언어 모델 로드
    # ----------------------------------------------------
    print(f"[bold blue]🧠 AI 언어 모델 로딩 중... ({cfg['model']['embedding']})[/bold blue]")
    model = SentenceTransformer(cfg['model']['embedding'], device=cfg['model']['device'])
    
    # 메모리 오버플로우 방지 및 네트워크 I/O 효율을 위한 배치 크기 설정
    batch_size = 256
    total_count = len(rows)
    print(f"🚀 본격적인 임베딩 및 Elasticsearch 전송을 시작합니다. (Batch Size: {batch_size})")

    start_time = time.time()
    
    # ----------------------------------------------------
    # 3. 데이터 청크(Batch) 단위 처리
    # ----------------------------------------------------
    for i in range(0, total_count, batch_size):
        batch = rows[i : i + batch_size]
        texts = []
        
        # 각 상품의 메타데이터를 하나의 풍부한 자연어 문장으로 조립 (AI 이해도 향상)
        for r in batch:
            txt = f"카테고리: {r['Category_Name']}, 캐릭터: {r['IP_Name']}. 상품명: {r['Products_Name']}. 설명: {r['Description']}."
            texts.append(txt)
        
        # GPU를 활용한 초고속 임베딩 연산 수행
        vectors = model.encode(texts, normalize_embeddings=True).tolist()
        
        # ----------------------------------------------------
        # 4. Elasticsearch Bulk API 페이로드(NDJSON 포맷) 조립
        # ----------------------------------------------------
        bulk_payload = ""
        for j, r in enumerate(batch):
            # 🎯 ES의 문서 식별자(_id) 규격을 준수하기 위해 str() 형변환 처리
            action = {"update": {"_index": INDEX_NAME, "_id": str(r[ID_FIELD])}}
            # doc_as_upsert=True: 기존 문서가 있으면 업데이트(벡터 갱신), 없으면 새로 삽입
            doc = {"doc": {VECTOR_FIELD: vectors[j]}, "doc_as_upsert": True}
            
            # ES Bulk API는 반드시 줄바꿈(\n)으로 각 명령을 구분해야 함 (NDJSON)
            bulk_payload += json.dumps(action) + "\n" + json.dumps(doc) + "\n"
        
        # ----------------------------------------------------
        # 5. HTTP POST 전송
        # ----------------------------------------------------
        try:
            res = requests.post(f"{ES_URL}/_bulk", data=bulk_payload, 
                                headers={"Content-Type": "application/x-ndjson"}, timeout=20)
            res.raise_for_status() # HTTP 오류 발생 시 Exception 발생
            print(f" ➔ [[bold green]{min(i+batch_size, total_count)}[/bold green]/{total_count}]건 전송 성공")
        except Exception as e:
            print(f"[bold red] ❌ ES 전송 에러 발생 ([{i}]번째 배치): {e}[/bold red]")
            try:
                # 에러 원인 파악을 위해 ES에서 응답한 상세 메시지 출력
                print(res.json()) 
            except:
                pass

    print(f"\n[bold green]✨ 전체 데이터 적재 완료! 총 {total_count}건 업데이트 (소요시간: {time.time() - start_time:.2f}초)[/bold green]\n")

if __name__ == "__main__":
    fetch_and_embed()