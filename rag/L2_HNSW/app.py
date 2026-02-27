"""
[L2_HNSW] FastAPI 비동기 RAG 검색 서버 (Ultra-Batch Mode)
FAISS HNSW (Hierarchical Navigable Small World) 그래프 기반 인덱스를 활용하여
대규모 데이터에서도 L1 대비 압도적으로 빠른 속도로 질문에 적합한 결과를 검색합니다.
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
import math
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

# 의도(Intent) 분류 카테고리 로드
CATEGORIES_PATH = Path("../data/categories.json")
INTENT_CATEGORIES = {}
if CATEGORIES_PATH.exists():
    with open(CATEGORIES_PATH, "r", encoding="utf-8") as f:
        INTENT_CATEGORIES = json.load(f)

# [Loguru 로깅 설정] 날짜별로 100MB 크기 제한, L2 전용 로그 파일명 사용
logger.add(
    "logs/rag_l2_server_{time:YYYY-MM-DD}.log", 
    rotation="100 MB",
    enqueue=True,
    format="{time:YYYY-MM-DD HH:mm:ss} | {level} | {message}"
)

# [GPU 모니터링 초기화]
try:
    pynvml.nvmlInit()
    GPU_AVAILABLE = True
    logger.info("✅ NVIDIA GPU 모니터링 활성화 완료.")
except:
    GPU_AVAILABLE = False
    logger.warning("⚠️ GPU가 감지되지 않았습니다. (CPU 모드로 작동 중)")

# 전역 객체 및 락
_index, _meta, _model, _reranker = None, [], None, None
_resource_lock = threading.Lock()
_is_syncing = False
_last_loaded_mtime = 0.0

# Ultra-Batch 비동기 큐
search_queue = asyncio.Queue()

# API 요청 스키마
class SearchRequest(BaseModel):
    q: str
    top_k: Optional[int] = None
    age_group: Optional[str] = None
    gender: Optional[str] = None

# ==========================================
# 🛠️ [1-2] 하드웨어 자원 모니터링 전담
# ==========================================
def fetch_hardware_status(last_sent, last_recv):
    """현재 시스템의 CPU, RAM, GPU, 네트워크 사용량을 반환합니다."""
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
            logger.info(f"[L2_HNSW][자원] CPU: {cpu:04.1f}% | RAM: {ram:04.1f}% | GPU: {gpu} | NET: ↑{mb_s:.2f}MB/s ↓{mb_r:.2f}MB/s | Model: {model_name}")
        except Exception as e:
            logger.error(f"자원 모니터링 오류: {e}")
        await asyncio.sleep(1.0)

# ==========================================
# 📡 [2] 리소스 로드 및 인덱스 감시(핫 리로드)
# ==========================================
def load_resources(full_load=True):
    """임베딩 모델과 L2 HNSW 인덱스를 적재합니다."""
    global _index, _meta, _model, _reranker, _last_loaded_mtime
    
    if full_load or _model is None:
        print(f"[bold blue][{datetime.now().strftime('%H:%M:%S')}] 🤖 AI 언어 모델 로딩 시작...[/bold blue]")
        _model = SentenceTransformer(cfg['model']['embedding'], device=cfg['model']['device'])
        _reranker = CrossEncoder(cfg['model']['reranker'], device=cfg['model']['device'])

    idx_path = Path(cfg['paths']['index_root']) / "index.faiss"
    meta_path = Path(cfg['paths']['meta_file'])
    
    if idx_path.exists() and meta_path.exists():
        current_mtime = os.path.getmtime(str(idx_path))
        
        new_index = None
        for attempt in range(3):
            try:
                new_index = faiss.read_index(str(idx_path))
                break
            except Exception as e:
                print(f"[bold yellow]⚠️ 인덱스 파일 접근 경합 발생! 재시도 중 ({attempt+1}/3)...[/bold yellow]")
                time.sleep(0.5)
        
        if new_index is None:
            print("[bold red]❌ L2 HNSW 인덱스 로드 최종 실패 (파일 손상 의심)[/bold red]")
            return

        with open(meta_path, "r", encoding="utf-8") as f:
            new_meta = [json.loads(line) for line in f]
            
        with _resource_lock:
            _index, _meta = new_index, new_meta
            _last_loaded_mtime = current_mtime
            
        print(f"[bold green][{datetime.now().strftime('%H:%M:%S')}] ✅ L2 HNSW 인덱스 & 메타데이터 성공적 적재 완료 (총 {len(_meta)}건)[/bold green]")

async def index_watcher():
    """인덱스 파일 변경 감지 시 자동으로 메모리 리로드를 트리거합니다."""
    idx_path = Path(cfg['paths']['index_root']) / "index.faiss"
    while True:
        await asyncio.sleep(5)
        if idx_path.exists() and os.path.getmtime(str(idx_path)) > _last_loaded_mtime:
            print(f"[bold yellow][{datetime.now().strftime('%H:%M:%S')}] 🔄 DB 동기화 감지됨: L2 무중단 리로드 진행 중...[/bold yellow]")
            load_resources(full_load=False)

# ==========================================
# 🧪 [3] 극한 최적화 비동기 배치 엔진 (핵심)
# ==========================================
async def batch_processor():
    """
    GPU 병렬 처리를 위한 거대 큐 대기열(Gather) 및 HNSW 검색 로직.
    안전한 확률 점수 변환과 공지사항 방어막이 포함된 실무형 부스팅을 수행합니다.
    """
    while True:
        first_item = await search_queue.get()
        items = [first_item]
        start_time = time.time()
        
        # 0.1초 동안 최대 16개의 요청을 모음
        while len(items) < 16 and (time.time() - start_time) < 0.1:
            try:
                items.append(search_queue.get_nowait())
            except asyncio.QueueEmpty:
                await asyncio.sleep(0.01)

        queries = [it['q'] for it in items]
        
        try:
            with _resource_lock:
                local_idx, local_meta, local_model, local_reranker = _index, _meta, _model, _reranker

            if not local_idx or not local_meta:
                for it in items: it['future'].set_result({"results": [], "intent": "GENERAL_CHAT"})
                continue

            # ----------------------------------------------------
            # [STEP 1] 의도(Intent) 분류 배치 처리
            # ----------------------------------------------------
            final_intents = ["GENERAL_CHAT"] * len(items)
            if INTENT_CATEGORIES:
                intent_keys = list(INTENT_CATEGORIES.keys())
                intent_values = list(INTENT_CATEGORIES.values())
                intent_pairs = [[q, v] for q in queries for v in intent_values]
                intent_scores = local_reranker.predict(intent_pairs, batch_size=len(intent_pairs))
                
                for i in range(len(items)):
                    start_idx = i * len(intent_values)
                    end_idx = start_idx + len(intent_values)
                    final_intents[i] = intent_keys[np.argmax(intent_scores[start_idx:end_idx])]

            # ----------------------------------------------------
            # [STEP 2] 질문 임베딩 배치 처리
            # ----------------------------------------------------
            q_vecs = local_model.encode(queries, normalize_embeddings=True, batch_size=len(queries)).astype("float32")
            
            # ----------------------------------------------------
            # [STEP 3] L2 HNSW 검색 및 IDSelector 필터링 우회 로직
            # ----------------------------------------------------
            all_candidates_per_item = []
            rerank_pairs = []
            item_candidate_counts = []

            for idx, item in enumerate(items):
                req_age = item.get('age_group')
                req_gender = item.get('gender')
                req_top_k = item.get('top_k') if item.get('top_k') else 5
                
                allow_list = []
                if req_age or req_gender:
                    for i, m in enumerate(local_meta):
                        t_age = m.get('target_age', '무관')
                        t_gen = m.get('target_gender', '무관')
                        match_a = (not req_age) or (req_age in t_age) or ("무관" in t_age)
                        match_g = (not req_gender) or (req_gender in t_gen) or ("무관" in t_gen)
                        if match_a and match_g: allow_list.append(i)

                candidates = []
                fetch_limit = req_top_k * 5

                # ⭐ HNSW 인덱스는 버전에 따라 IDSelector를 네이티브로 지원하지 않을 수 있음
                if req_age or req_gender:
                    try:
                        # 1순위: HNSW 전용 SearchParameters 시도
                        sel = faiss.IDSelectorBatch(allow_list)
                        params = faiss.SearchParametersHNSW(sel=sel)
                        D, I = local_idx.search(q_vecs[idx:idx+1], fetch_limit, params=params)
                        candidates = [local_meta[i] for i in I[0] if 0 <= i < len(local_meta)]
                    except Exception:
                        # 2순위: 파이썬 단 후처리 (우회)
                        D, I = local_idx.search(q_vecs[idx:idx+1], len(local_meta))
                        for i in I[0]:
                            if 0 <= i < len(local_meta) and i in allow_list:
                                candidates.append(local_meta[i])
                                if len(candidates) >= fetch_limit: break
                else:
                    D, I = local_idx.search(q_vecs[idx:idx+1], fetch_limit)
                    candidates = [local_meta[i] for i in I[0] if 0 <= i < len(local_meta)]

                all_candidates_per_item.append(candidates)
                item_candidate_counts.append(len(candidates))
                
                for cand in candidates:
                    rerank_pairs.append([queries[idx], cand['text']])

            # ----------------------------------------------------
            # [STEP 4] 크로스 인코더 리랭킹 
            # ----------------------------------------------------
            all_scores = []
            if rerank_pairs:
                all_scores = local_reranker.predict(rerank_pairs, batch_size=len(rerank_pairs))

            # ----------------------------------------------------
            # [STEP 5] 실무형 타겟 가중합 및 공지사항 방어 로직
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
                    # ⭐ 확률 왜곡 방지: Sigmoid 없이 순수 점수만 사용하고, 음수는 0으로 보정
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

                    # ⭐ 방어 2: 공지/QnA이거나, 관련성 30% 미만 상품은 가중치 완전 배제
                    if source_table != "Products" or semantic_prob < 0.3:  
                        final_score = semantic_prob
                    else:
                        # 통과된 상품에 한해 [의미 유사도 85% + 타겟일치도 15%] 가중합 적용
                        final_score = (semantic_prob * 0.85) + (target_score * 0.15)

                    if final_score >= cfg['search']['score_threshold']:
                        res_item = {
                            "score": float(final_score), 
                            "source": cand.get('source')
                        }
                        
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
                        
                        if len(res_item) > 3: 
                            final_results.append(res_item)

                # 최종 산출된 가중합 점수 기준으로 정렬
                final_results.sort(key=lambda x: x['score'], reverse=True)
                req_top_k = item['top_k'] if item['top_k'] else 5
                
                process_time = time.time() - start_time
                top_score = final_results[0]['score'] if final_results else 0.0
                short_q = queries[idx][:15].replace('\n', '') + "..."
                logger.info(f"[L2_HNSW][검색완료] 질의: '{short_q}' | 의도: {final_intents[idx]} | 최고점수: {top_score:.4f} | 소요시간: {process_time:.3f}s | 반환: {len(final_results[:req_top_k])}건")
                
                item['future'].set_result({"intent": final_intents[idx], "results": final_results[:req_top_k]})

        except Exception as e:
            print(f"[bold red]❌ L2 배치 처리 오류: {e}[/bold red]")
            logger.error(f"L2 Batch Error: {e}")
            for it in items:
                if not it['future'].done(): it['future'].set_exception(e)

# ==========================================
# 🚀 [4] FastAPI 라이프사이클 및 엔드포인트
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    load_resources(full_load=True)
    p_task = asyncio.create_task(batch_processor())
    w_task = asyncio.create_task(index_watcher())
    m_task = asyncio.create_task(resource_monitor())
    yield
    p_task.cancel()
    w_task.cancel()
    m_task.cancel()

app = FastAPI(title="GutJJeu RAG L2_HNSW Optimized Server", lifespan=lifespan)

@app.post("/search")
async def search(req: SearchRequest):
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    await search_queue.put({"q": req.q, "top_k": req.top_k, "age_group": req.age_group, "gender": req.gender, "future": future})
    return await future

@app.post("/sync-item/{source_id}")
async def trigger_item_sync(source_id: str):
    def notify_l6():
        try:
            import requests
            res = requests.post(f"http://localhost:8078/sync-item/{source_id}", timeout=5)
            print(f"✅ [L6 ES] 상위 노드(Elasticsearch) 전파 성공")
        except Exception: pass
    threading.Thread(target=notify_l6, daemon=True).start()

    global _is_syncing
    if _is_syncing: return {"message": "동기화 진행 중입니다."}
    
    def task():
        global _is_syncing
        _is_syncing = True
        try:
            import db_sync
            print(f"[magenta]🔄 '{source_id}' 데이터의 실시간 단건 동기화를 시작합니다...[/magenta]")
            db_sync.run_sync(target_source_id=source_id)
            load_resources(full_load=False)
        finally: _is_syncing = False
    threading.Thread(target=task).start()
    return {"message": f"'{source_id}' 통합 동기화 예약됨"}

if __name__ == "__main__":
    config = Config()
    config.bind = ["0.0.0.0:8077"] 
    config.workers = 1 # 메모리 사용량이 높은 HNSW 특성상 워커는 1로 제한 권장
    config.alpn_protocols = ["h2", "http/1.1"]
    print("[bold red]🔥 GutJJeu RAG L2_HNSW Optimized Server 구동 완료! (Worker: 1)[/bold red]")
    asyncio.run(serve(app, config))