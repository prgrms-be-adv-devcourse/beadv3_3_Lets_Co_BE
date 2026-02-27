"""
[L1_Flat] FastAPI 비동기 RAG 검색 서버 (Ultra-Batch Mode)
FAISS Flat 인덱스를 활용하여 사용자의 질문에 가장 적합한 상품 및 게시글을 검색하고 반환합니다.
개인화 타겟(나이/성별) 스마트 가중합 로직과, 실시간 무중단 리로드(Watcher) 기능이 포함되어 있습니다.
"""

import threading
import json
import yaml
import faiss
import torch
import asyncio
import numpy as np
import time
import os
import psutil
import pynvml
from loguru import logger
from pathlib import Path
from datetime import datetime
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from sentence_transformers import SentenceTransformer, CrossEncoder
from rich import print
from hypercorn.config import Config
from hypercorn.asyncio import serve
from contextlib import asynccontextmanager

# ==========================================
# ⚙️ [1] 환경 설정 및 전역 자원 초기화
# ==========================================
CONFIG_PATH = Path("../rag_config.yml")
with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

# 의도(Intent) 분류를 위한 카테고리 데이터 로드
CATEGORIES_PATH = Path("../data/categories.json")
INTENT_CATEGORIES = {}
if CATEGORIES_PATH.exists():
    with open(CATEGORIES_PATH, "r", encoding="utf-8") as f:
        INTENT_CATEGORIES = json.load(f)

# [Loguru 로깅 설정] 날짜별로 100MB 크기 제한을 두어 비동기(enqueue) 저장
logger.add(
    "logs/rag_server_{time:YYYY-MM-DD}.log", 
    rotation="100 MB",
    enqueue=True,
    format="{time:YYYY-MM-DD HH:mm:ss} | {level} | {message}"
)

# [GPU 모니터링 초기화] NVIDIA 드라이버 상태 확인
try:
    pynvml.nvmlInit()
    GPU_AVAILABLE = True
    logger.info("✅ NVIDIA GPU 모니터링 활성화 완료.")
except:
    GPU_AVAILABLE = False
    logger.warning("⚠️ GPU가 감지되지 않았거나 NVML 초기화에 실패했습니다. (CPU 모드로 작동 가능성 있음)")

# 전역 객체 (인덱스, 메타데이터, AI 모델들) 및 쓰레드 락
_index, _meta, _model, _reranker = None, [], None, None
_resource_lock = threading.Lock() # 핫 리로드 시 쿼리 간 충돌을 방지하는 락
_is_syncing = False
_last_loaded_mtime = 0.0

# Ultra-Batch 처리를 위한 비동기 큐
search_queue = asyncio.Queue()

# API 요청 스키마 모델
class SearchRequest(BaseModel):
    q: str
    top_k: Optional[int] = None
    age_group: Optional[str] = None
    gender: Optional[str] = None

# ==========================================
# 🛠️ [1-2] 하드웨어 자원 모니터링 전담
# ==========================================
def fetch_hardware_status(last_sent, last_recv):
    """현재 시스템의 CPU, RAM, GPU, 네트워크 사용량을 측정하여 반환합니다."""
    cpu_util = psutil.cpu_percent(interval=None)
    ram_util = psutil.virtual_memory().percent
    
    gpu_info = "N/A"
    if GPU_AVAILABLE:
        handle = pynvml.nvmlDeviceGetHandleByIndex(0)
        mem_info = pynvml.nvmlDeviceGetMemoryInfo(handle)
        gpu_util = pynvml.nvmlDeviceGetUtilizationRates(handle).gpu
        vram_util = (mem_info.used / mem_info.total) * 100
        gpu_info = f"{gpu_util}% (VRAM: {vram_util:.1f}%)"

    net_curr = psutil.net_io_counters()
    mb_sent = (net_curr.bytes_sent - last_sent) / 1024 / 1024
    mb_recv = (net_curr.bytes_recv - last_recv) / 1024 / 1024
    
    return cpu_util, ram_util, gpu_info, mb_sent, mb_recv, net_curr.bytes_sent, net_curr.bytes_recv

async def resource_monitor():
    """백그라운드에서 매초 단위로 서버 하드웨어 상태를 로깅합니다."""
    net_init = psutil.net_io_counters()
    last_sent = net_init.bytes_sent
    last_recv = net_init.bytes_recv
    model_name = cfg['model']['embedding'].split("/")[-1]

    while True:
        try:
            cpu, ram, gpu, mb_s, mb_r, last_sent, last_recv = await asyncio.to_thread(
                fetch_hardware_status, last_sent, last_recv
            )
            # 상태 디버깅 시 로그 레벨을 debug로 낮출 수 있으나, 가시성을 위해 info 유지
            logger.info(f"[L1_Flat][자원] CPU: {cpu:04.1f}% | RAM: {ram:04.1f}% | GPU: {gpu} | NET: ↑{mb_s:.2f}MB/s ↓{mb_r:.2f}MB/s | Model: {model_name}")
        except Exception as e:
            logger.error(f"자원 모니터링 오류: {e}")
        await asyncio.sleep(1.0)

# ==========================================
# 📡 [2] 리소스 로드 및 인덱스 감시(핫 리로드)
# ==========================================
def load_resources(full_load=True):
    """
    임베딩 모델, 리랭킹 모델, FAISS 인덱스 및 메타데이터를 메모리에 적재합니다.
    full_load가 False일 경우 모델은 유지하고 파일(인덱스/메타)만 빠르게 교체합니다.
    """
    global _index, _meta, _model, _reranker, _last_loaded_mtime
    
    if full_load or _model is None:
        print(f"[bold blue][{datetime.now().strftime('%H:%M:%S')}] 🤖 AI 언어 모델(임베딩/리랭커) 로딩 시작...[/bold blue]")
        _model = SentenceTransformer(cfg['model']['embedding'], device=cfg['model']['device'])
        _reranker = CrossEncoder(cfg['model']['reranker'], device=cfg['model']['device'])

    idx_path = Path(cfg['paths']['index_root']) / "index.faiss"
    meta_path = Path(cfg['paths']['meta_file'])
    
    if idx_path.exists() and meta_path.exists():
        current_mtime = os.path.getmtime(str(idx_path))
        
        # 파일 쓰기 작업(sync)과 겹칠 경우를 대비한 재시도 로직
        new_index = None
        for attempt in range(3):
            try:
                new_index = faiss.read_index(str(idx_path))
                break
            except Exception as e:
                print(f"[bold yellow]⚠️ 인덱스 파일 접근 경합 발생! 재시도 중 ({attempt+1}/3)...[/bold yellow]")
                time.sleep(0.5)
        
        if new_index is None:
            print("[bold red]❌ FAISS 인덱스 로드 최종 실패 (파일 손상 의심)[/bold red]")
            return

        with open(meta_path, "r", encoding="utf-8") as f:
            new_meta = [json.loads(line) for line in f]
            
        # 쓰레드 안전(Thread-Safe)하게 전역 객체 교체
        with _resource_lock:
            _index, _meta = new_index, new_meta
            _last_loaded_mtime = current_mtime
            
        print(f"[bold green][{datetime.now().strftime('%H:%M:%S')}] ✅ FAISS 인덱스 & 메타데이터 성공적 적재 완료 (총 {len(_meta)}건)[/bold green]")

async def index_watcher():
    """백그라운드에서 인덱스 파일의 수정 시간(Mtime)을 감시하여, 변경 시 자동으로 메모리 리로드를 트리거합니다."""
    idx_path = Path(cfg['paths']['index_root']) / "index.faiss"
    while True:
        await asyncio.sleep(5)
        if idx_path.exists():
            current_mtime = os.path.getmtime(str(idx_path))
            if current_mtime > _last_loaded_mtime:
                print(f"[bold yellow][{datetime.now().strftime('%H:%M:%S')}] 🔄 데이터베이스 동기화 감지됨: 무중단 리로드(Hot-Reload) 진행 중...[/bold yellow]")
                load_resources(full_load=False)

# ==========================================
# 🧪 [3] 극한 최적화 비동기 배치 엔진 (핵심)
# ==========================================
async def batch_processor():
    """
    여러 사용자의 요청을 0.1초 동안 모아서 GPU에서 한 번에 병렬 연산(Batch Processing)하여 처리 속도를 극대화합니다.
    """
    while True:
        # 첫 번째 요청을 받고, 0.1초 동안 최대 16개까지 추가로 대기(Gather)
        first_item = await search_queue.get()
        items = [first_item]
        start_time = time.time()
        
        while len(items) < 16 and (time.time() - start_time) < 0.1:
            try:
                next_item = search_queue.get_nowait()
                items.append(next_item)
            except asyncio.QueueEmpty:
                await asyncio.sleep(0.01)

        queries = [it['q'] for it in items]
        
        try:
            with _resource_lock:
                local_idx, local_meta, local_model, local_reranker = _index, _meta, _model, _reranker

            if not local_idx or not local_meta:
                # 인덱스가 비어있는 경우 기본 응답 반환
                for it in items: it['future'].set_result({"results": [], "intent": "GENERAL_CHAT"})
                continue

            # ----------------------------------------------------
            # [STEP 1] 의도(Intent) 분류 배치 처리 (리랭커 활용)
            # ----------------------------------------------------
            final_intents = ["GENERAL_CHAT"] * len(items)
            if INTENT_CATEGORIES:
                intent_keys = list(INTENT_CATEGORIES.keys())
                intent_values = list(INTENT_CATEGORIES.values())
                # 모든 질문 x 모든 카테고리 조합 생성
                intent_pairs = [[q, v] for q in queries for v in intent_values]
                intent_scores = local_reranker.predict(intent_pairs, batch_size=len(intent_pairs))
                
                # 각 질문별 최고 점수를 받은 카테고리 할당
                for i in range(len(items)):
                    start_idx = i * len(intent_values)
                    end_idx = start_idx + len(intent_values)
                    final_intents[i] = intent_keys[np.argmax(intent_scores[start_idx:end_idx])]

            # ----------------------------------------------------
            # [STEP 2] 질문 임베딩(Vector화) 배치 처리
            # ----------------------------------------------------
            q_vecs = local_model.encode(queries, normalize_embeddings=True, batch_size=len(queries)).astype("float32")
            
            # ----------------------------------------------------
            # [STEP 3] FAISS 검색 및 사전 타겟 필터링 (Pre-filtering)
            # ----------------------------------------------------
            all_candidates_per_item = []
            rerank_pairs = []
            item_candidate_counts = []

            for idx, item in enumerate(items):
                req_age = item.get('age_group')
                req_gender = item.get('gender')
                req_top_k = item.get('top_k') if item.get('top_k') else 5
                
                # 유저 프로필이 있을 경우 검색 풀(Allow List) 구성
                allow_list = []
                if req_age or req_gender:
                    for i, m in enumerate(local_meta):
                        t_age = m.get('target_age', '무관')
                        t_gen = m.get('target_gender', '무관')
                        
                        # 나이 또는 성별이 맞거나, '무관'인 공통 데이터만 허용
                        match_a = (not req_age) or (req_age in t_age) or ("무관" in t_age)
                        match_g = (not req_gender) or (req_gender in t_gen) or ("무관" in t_gen)
                        
                        if match_a and match_g:
                            allow_list.append(i)

                candidates = []
                # 리랭킹에서 깎일 것을 대비해 5배수 확보
                fetch_limit = req_top_k * 5 

                if req_age or req_gender:
                    try:
                        # 전략 1: FAISS 내장 IDSelector를 통한 고속 필터링 (Flat에서 지원될 경우)
                        sel = faiss.IDSelectorBatch(allow_list)
                        params = faiss.SearchParametersIVF(sel=sel)
                        D, I = local_idx.search(q_vecs[idx:idx+1], fetch_limit, params=params)
                        candidates = [local_meta[i] for i in I[0] if 0 <= i < len(local_meta)]
                    except Exception:
                        # 전략 2 (우회): IDSelector 지원 안될 시, 전체 검색 후 파이썬 단에서 직접 슬라이싱
                        D, I = local_idx.search(q_vecs[idx:idx+1], len(local_meta))
                        for i in I[0]:
                            if 0 <= i < len(local_meta) and i in allow_list:
                                candidates.append(local_meta[i])
                                if len(candidates) >= fetch_limit:
                                    break
                else:
                    # 유저 프로필이 없는 경우(비로그인) 필터 없이 전체 검색
                    D, I = local_idx.search(q_vecs[idx:idx+1], fetch_limit)
                    candidates = [local_meta[i] for i in I[0] if 0 <= i < len(local_meta)]

                all_candidates_per_item.append(candidates)
                item_candidate_counts.append(len(candidates))
                
                # 리랭킹을 위한 텍스트 페어 생성
                for cand in candidates:
                    rerank_pairs.append([queries[idx], cand['text']])

            # ----------------------------------------------------
            # [STEP 4] 크로스 인코더 리랭킹 배치 처리 (정밀 의미 평가)
            # ----------------------------------------------------
            all_scores = []
            if rerank_pairs:
                all_scores = local_reranker.predict(rerank_pairs, batch_size=len(rerank_pairs))

            # ----------------------------------------------------
            # [STEP 5] 실무형 타겟 가중합 및 응답 데이터 조립 (방어 로직 포함)
            # ----------------------------------------------------
            score_cursor = 0
            for idx, item in enumerate(items):
                count = item_candidate_counts[idx]
                item_scores = all_scores[score_cursor : score_cursor + count]
                score_cursor += count
                
                candidates = all_candidates_per_item[idx]
                final_results = []
                
                req_age = item.get('age_group')
                req_gender = item.get('gender')
                
                for i, cand in enumerate(candidates):
                    # AI 모델이 내뱉은 순수 유사도(확률) 점수 추출 (음수 보정)
                    semantic_prob = float(item_scores[i])
                    if semantic_prob < 0: semantic_prob = 0.0
                    
                    source_table = cand.get('source', '').split(":")[0]
                    target_score = 0.0

                    # ⭐ 방어 1: 오직 "상품(Products)"일 경우에만 타겟 가중치를 계산합니다.
                    if source_table == "Products":
                        cand_age = cand.get('target_age', ["무관"])
                        cand_gen = cand.get('target_gender', ["무관"])
                        # 조건당 0.5점씩 가산
                        if req_age and req_age in cand_age: target_score += 0.5
                        if req_gender and req_gender in cand_gen: target_score += 0.5

                    # ⭐ 방어 2: 공지/QnA이거나, 상품이더라도 의미 연관성이 30점 미만이면 가중치를 주지 않고 버림.
                    if source_table != "Products" or semantic_prob < 0.3:  
                        final_score = semantic_prob
                    else:
                        # 질문과 연관성이 있는 상품에 한해서만 가중합(의미 85% : 개인화 15%) 적용
                        final_score = (semantic_prob * 0.85) + (target_score * 0.15)

                    # config.yml의 임계값(Threshold)을 넘는 데이터만 최종 결과로 포함
                    if final_score >= cfg['search']['score_threshold']:
                        res_item = {
                            "score": float(final_score), 
                            "source": cand.get('source')
                        }
                        
                        # 테이블별 포맷팅
                        if source_table == "Products":
                            res_item.update({
                                "Link": f"https://gutjjeu.chlab.org/products/{cand.get('Products_Code')}",
                                "Products_Name": cand.get("Products_Name"),
                                "Description": cand.get("Description"),
                                "Category_Name": cand.get("Category_Name"),
                                "IP_Name": cand.get("IP_Name"),
                                "Price": cand.get("Price"),
                                "Sale_Price": cand.get("Sale_Price"),
                                "View_Count": cand.get("View_Count"),
                                "Order_Count": cand.get("Order_Count"),
                                "Review": cand.get("Review_Count"),
                                "Option": cand.get("Options", []),
                                "Target_Info": f"{','.join(cand.get('target_age', ['무관']))} / {','.join(cand.get('target_gender', ['무관']))}"
                            })
                        elif source_table == "Customer_Service":
                            cs_type = cand.get("Type")
                            code = cand.get("Customer_Service_Code", "")
                            if cs_type == "NOTICE":
                                res_item.update({
                                    "Link": f"https://gutjjeu.chlab.org/board/notice/{code}",
                                    "Title": cand.get("Title"),
                                    "Category": cand.get("Category"),
                                    "Content": cand.get("Question", cand.get("text")),
                                    "View_Count": cand.get("View_Count"),
                                    "Is_Pinned": cand.get("Is_Pinned"),
                                    "Published_at": cand.get("Published_at")
                                })
                            elif cs_type in ["QNA_ADMIN", "QNA_PRODUCT"]:
                                res_item.update({
                                    "Link": f"https://gutjjeu.chlab.org/board/inquiry/{code}",
                                    "Products_Name": cand.get("Products_Name", "일반 문의"),
                                    "Title": cand.get("Title"),
                                    "Category": cand.get("Category"),
                                    "Status": cand.get("Status"),
                                    "Question": cand.get("Question", cand.get("text")),
                                    "Answer": cand.get("Answer"),
                                    "Created_at": cand.get("Created_at")
                                })
                        
                        # 필수 항목이 세팅된 유효한 데이터만 리스트에 편입
                        if len(res_item) > 3: 
                            final_results.append(res_item)

                # 최종 산출된 점수를 기준으로 내림차순 정렬
                final_results.sort(key=lambda x: x['score'], reverse=True)
                req_top_k = item['top_k'] if item['top_k'] else 5
                
                # 로그 출력용 소요시간 및 텍스트 정리
                process_time = time.time() - start_time
                top_score = final_results[0]['score'] if final_results else 0.0
                short_q = queries[idx][:15].replace('\n', '') + "..."
                
                logger.info(f"[L1_Flat][검색완료] 질의: '{short_q}' | 의도: {final_intents[idx]} | 최고점수: {top_score:.4f} | 소요시간: {process_time:.3f}s | 반환건수: {len(final_results[:req_top_k])}건")
                
                # 비동기 Future 객체에 결과 셋팅하여 엔드포인트로 응답 전달
                item['future'].set_result({"intent": final_intents[idx], "results": final_results[:req_top_k]})

        except Exception as e:
            print(f"[bold red]❌ 배치 처리 중 치명적 오류 발생: {e}[/bold red]")
            logger.error(f"Batch Processing Error: {e}")
            # 에러 발생 시 대기 중인 모든 요청에 예외 처리 전달 (무한 대기 방지)
            for it in items:
                if not it['future'].done(): it['future'].set_exception(e)

# ==========================================
# 🚀 [4] FastAPI 라이프사이클 및 라우터 설정
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 구동 시 필요한 자원을 로드하고 백그라운드 태스크를 실행합니다."""
    load_resources(full_load=True)
    
    # 3대 핵심 백그라운드 워커 구동
    p_task = asyncio.create_task(batch_processor())   # 검색 엔진
    w_task = asyncio.create_task(index_watcher())     # 파일 리로드 감시
    m_task = asyncio.create_task(resource_monitor())  # 하드웨어 감시
    
    yield
    
    # 서버 종료 시 안전하게 워커 취소
    p_task.cancel()
    w_task.cancel()
    m_task.cancel()

app = FastAPI(title="GutJJeu RAG L1 Ultra-Batch Server", lifespan=lifespan)

@app.post("/search")
async def search(req: SearchRequest):
    """Java 백엔드(또는 클라이언트)로부터의 검색 요청을 받아 처리합니다."""
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    # 요청을 큐에 넣고 배치 프로세서가 결과를 넣어줄 때까지 비동기 대기
    await search_queue.put({"q": req.q, "top_k": req.top_k, "age_group": req.age_group, "gender": req.gender, "future": future})
    return await future

@app.post("/sync-item/{source_id}")
async def trigger_item_sync(source_id: str):
    """
    특정 데이터(상품, 문의 등)에 변경이 일어났을 때 단건 동기화를 지시합니다.
    동시에 상위 L6(Elasticsearch) 노드에도 동기화 명령을 전파합니다.
    """
    def notify_l6():
        try:
            import requests
            res = requests.post(f"http://localhost:8078/sync-item/{source_id}", timeout=5)
            print(f"✅ [L6 ES] 상위 노드(Elasticsearch)에 '{source_id}' 업데이트 요청 전달 성공")
        except Exception as e:
            print(f"[yellow]⚠️ [L6 ES] 상위 노드 전달 실패 (L6 서버가 꺼져있을 수 있습니다): {e}[/yellow]")
            
    # L6 전파는 별도 쓰레드로 논블로킹 처리
    threading.Thread(target=notify_l6, daemon=True).start()

    global _is_syncing
    if _is_syncing: return {"message": "이미 동기화 작업이 진행 중입니다. (요청 무시됨)"}
    
    def task():
        global _is_syncing
        _is_syncing = True
        try:
            import db_sync
            print(f"[magenta]🔄 '{source_id}' 데이터의 실시간 단건 동기화를 시작합니다...[/magenta]")
            db_sync.run_sync(target_source_id=source_id)
            # 동기화 완료 후 메모리 리로드 유도
            load_resources(full_load=False)
        finally: 
            _is_syncing = False
            
    # 본인(L1) 동기화도 별도 쓰레드로 백그라운드 처리하여 API 응답 지연 방지
    threading.Thread(target=task).start()
    return {"message": f"'{source_id}' 통합 동기화(FAISS + ES) 작업이 백그라운드에서 예약되었습니다."}

if __name__ == "__main__":
    config = Config()
    config.bind = ["0.0.0.0:8077"]
    config.workers = 1 # 멀티 프로세스 워커 구동
    config.alpn_protocols = ["h2", "http/1.1"]
    print("[bold red]🔥 GutJJeu RAG L1_Flat Optimized Server 구동 완료! (Worker: 1, Ultra-Batch Mode 활성)[/bold red]")
    asyncio.run(serve(app, config))