"""
[L3_IVFPQ] FastAPI 비동기 RAG 검색 서버 (Ultra-Batch Mode)
압축된 IVF-PQ 인덱스를 활용하여 극도의 메모리 효율로 초고속 검색을 수행합니다.
nprobe(인접 클러스터 탐색 개수) 파라미터를 통해 속도와 정확도의 트레이드오프를 동적으로 제어합니다.
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
    
# 의도 분류 데이터 로드
CATEGORIES_PATH = Path("../data/categories.json")
INTENT_CATEGORIES = {}
if CATEGORIES_PATH.exists():
    with open(CATEGORIES_PATH, "r", encoding="utf-8") as f:
        INTENT_CATEGORIES = json.load(f)

# [Loguru 로깅 설정] L3 전용 로그 파일명 지정
logger.add(
    "logs/rag_l3_server_{time:YYYY-MM-DD}.log", 
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

# 전역 자원 관리
_index, _meta, _model, _reranker = None, [], None, None
_resource_lock = threading.Lock()
_is_syncing = False
_last_loaded_mtime = 0.0

# 비동기 검색 요청 큐
search_queue = asyncio.Queue()

# API 요청 스키마
class SearchRequest(BaseModel):
    q: str
    top_k: Optional[int] = None
    age_group: Optional[str] = None
    gender: Optional[str] = None

# ==========================================
# 🛠️ [1-2] 하드웨어 모니터링 전담 함수
# ==========================================
def fetch_hardware_status(last_sent, last_recv):
    """현재 시스템의 리소스(CPU, RAM, GPU, Net) 사용 상태를 반환합니다."""
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
    """백그라운드에서 매초 단위로 서버 리소스를 감시하고 로깅합니다."""
    net_init = psutil.net_io_counters()
    last_sent = net_init.bytes_sent
    last_recv = net_init.bytes_recv
    model_name = cfg['model']['embedding'].split("/")[-1]

    while True:
        try:
            cpu, ram, gpu, mb_s, mb_r, last_sent, last_recv = await asyncio.to_thread(
                fetch_hardware_status, last_sent, last_recv
            )
            logger.info(f"[L3_IVFPQ][자원] CPU: {cpu:04.1f}% | RAM: {ram:04.1f}% | GPU: {gpu} | NET: ↑{mb_s:.2f}MB/s ↓{mb_r:.2f}MB/s | Model: {model_name}")
        except Exception as e:
            logger.error(f"자원 모니터링 오류: {e}")
        await asyncio.sleep(1.0)

# ==========================================
# 📡 [2] 리소스 로드 및 파일 감시(Watcher) 로직
# ==========================================
def load_resources(full_load=True):
    """
    임베딩 모델과 L3 IVF-PQ 인덱스를 메모리에 적재합니다.
    적재 시 인덱스에 nprobe(정밀도) 파라미터를 동적으로 주입합니다.
    """
    global _index, _meta, _model, _reranker, _last_loaded_mtime
    
    if full_load or _model is None:
        print(f"[bold blue][{datetime.now().strftime('%H:%M:%S')}] 🤖 L3 AI 모델 로딩 시작... (Device: {cfg['model']['device']})[/bold blue]")
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
                print(f"[bold yellow]⚠️ L3 인덱스 읽기 경합! 재시도 중 ({attempt+1}/3)...[/bold yellow]")
                time.sleep(0.5)
                
        if new_index is None:
            print("[bold red]❌ L3 인덱스 로드 최종 실패[/bold red]")
            return
            
        # ⭐ [L3 전용 최적화] IVF 인덱스의 탐색 반경(nprobe) 설정
        # 값이 클수록 정확도는 올라가지만 속도가 느려집니다. config에서 동적 제어.
        if hasattr(new_index, 'nprobe'):
            nprobe_val = cfg['index_params']['ivf_nprobe']
            new_index.nprobe = nprobe_val
            print(f"[bold cyan]🔍 L3 검색 정밀도(nprobe) 설정됨: {nprobe_val}[/bold cyan]")

        with open(meta_path, "r", encoding="utf-8") as f:
            new_meta = [json.loads(line) for line in f]
            
        with _resource_lock:
            _index, _meta = new_index, new_meta
            _last_loaded_mtime = current_mtime
            
        print(f"[bold green][{datetime.now().strftime('%H:%M:%S')}] ✅ L3 IVF-PQ 리소스 로드 완료 (총 {len(_meta)}건)[/bold green]")

async def index_watcher():
    """인덱스 파일 변경 감지 시 자동으로 메모리를 교체합니다."""
    idx_path = Path(cfg['paths']['index_root']) / "index.faiss"
    while True:
        await asyncio.sleep(5)
        if idx_path.exists():
            current_mtime = os.path.getmtime(str(idx_path))
            if current_mtime > _last_loaded_mtime:
                print(f"[bold yellow][{datetime.now().strftime('%H:%M:%S')}] 🔄 DB 갱신 감지됨: 새 인덱스를 핫 리로드합니다...[/bold yellow]")
                load_resources(full_load=False)

# ==========================================
# 🧪 [3] L3 IVF-PQ Ultra-Batch 처리 엔진
# ==========================================
async def batch_processor():
    """
    FAISS IVF-PQ를 이용한 고속 배치 검색과, 의미 훼손을 방지하는 상용 등급의 타겟 가중합을 처리합니다.
    """
    while True:
        first_item = await search_queue.get()
        items = [first_item]

        start_time = time.time()
        # 0.1초 안에 들어오는 다중 요청을 묶어서(Gather) 병렬 처리
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
            # [STEP 1] 의도 분류 배치 처리
            # ----------------------------------------------------
            final_intents = ["GENERAL_CHAT"] * len(items)
            if INTENT_CATEGORIES:
                intent_keys = list(INTENT_CATEGORIES.keys())
                intent_values = list(INTENT_CATEGORIES.values())
                intent_pairs = [[q, v] for q in queries for v in intent_values]
                intent_scores = local_reranker.predict(intent_pairs, batch_size=len(intent_pairs))
                for i in range(len(items)):
                    s_idx = i * len(intent_values)
                    e_idx = s_idx + len(intent_values)
                    final_intents[i] = intent_keys[np.argmax(intent_scores[s_idx:e_idx])]

            # ----------------------------------------------------
            # [STEP 2] 임베딩 배치 처리
            # ----------------------------------------------------
            q_vecs = local_model.encode(queries, normalize_embeddings=True, batch_size=len(queries)).astype("float32")
            
            # ----------------------------------------------------
            # [STEP 3] 메타데이터 필터링 (IVF-PQ) 및 검색
            # ----------------------------------------------------
            all_candidates_per_item = []
            rerank_pairs = []
            item_candidate_counts = []

            for idx, item in enumerate(items):
                req_age = item.get('age_group')
                req_gender = item.get('gender')
                req_top_k = item.get('top_k') if item.get('top_k') else 5
                
                allow_list = set()
                if req_age or req_gender:
                    for i, m in enumerate(local_meta):
                        t_age = m.get('target_age', '무관')
                        t_gen = m.get('target_gender', '무관')
                        
                        match_a = (not req_age) or (req_age in t_age) or ("무관" in t_age)
                        match_g = (not req_gender) or (req_gender in t_gen) or ("무관" in t_gen)
                        
                        if match_a and match_g:
                            allow_list.add(i)

                candidates = []
                fetch_limit = req_top_k * 5
                
                if req_age or req_gender:
                    try:
                        # 🎯 전략 1: FAISS IDSelector 시도 (IVF 계열은 보통 지원됨)
                        sel = faiss.IDSelectorBatch(list(allow_list))
                        params = faiss.SearchParametersIVF(sel=sel)
                        D, I = local_idx.search(q_vecs[idx:idx+1], fetch_limit, params=params)
                        candidates = [local_meta[i] for i in I[0] if 0 <= i < len(local_meta)]
                    except Exception:
                        # 🎯 전략 2 (우회): 실패 시 넉넉하게(2배수) 추출 후 파이썬 단 필터링
                        search_k = min(fetch_limit * 2, len(local_meta)) 
                        D, I = local_idx.search(q_vecs[idx:idx+1], search_k)
                        for i in I[0]:
                            if 0 <= i < len(local_meta) and i in allow_list:
                                candidates.append(local_meta[i])
                                if len(candidates) >= fetch_limit:
                                    break
                else:
                    D, I = local_idx.search(q_vecs[idx:idx+1], fetch_limit)
                    candidates = [local_meta[i] for i in I[0] if 0 <= i < len(local_meta)]
                
                all_candidates_per_item.append(candidates)
                item_candidate_counts.append(len(candidates))
                
                for cand in candidates:
                    rerank_pairs.append([queries[idx], cand['text']])

            # ----------------------------------------------------
            # [STEP 4] 리랭킹 거대 배치 처리 (압축된 벡터의 점수 보정)
            # ----------------------------------------------------
            all_scores = []
            if rerank_pairs:
                all_scores = local_reranker.predict(rerank_pairs, batch_size=len(rerank_pairs))

            # ----------------------------------------------------
            # [STEP 5] 실무형 타겟 가중합 로직 (공지사항 보호 적용)
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
                    # ⭐ 확률 왜곡 방지: AI 확률 점수를 그대로 사용, 음수는 0 처리
                    semantic_prob = float(item_scores[i])
                    if semantic_prob < 0: semantic_prob = 0.0
                    
                    source_table = cand.get('source', '').split(":")[0]
                    target_score = 0.0

                    # 1. 오직 "상품(Products)"일 경우에만 타겟 가중치를 계산합니다.
                    if source_table == "Products":
                        cand_age = cand.get('target_age', ["무관"])
                        cand_gen = cand.get('target_gender', ["무관"])
                        # 조건당 0.5점씩 가산
                        if req_age and req_age in cand_age: target_score += 0.5
                        if req_gender and req_gender in cand_gen: target_score += 0.5

                    # 2. 공지/QnA이거나, 상품이더라도 연관성(semantic_prob)이 0.3 미만이면 가중치 배제
                    if source_table != "Products" or semantic_prob < 0.3:
                        final_score = semantic_prob
                    else:
                        # 통과된 관련 상품에 한해서만 실무형 가중합(의미 85% : 개인화 15%) 적용
                        final_score = (semantic_prob * 0.85) + (target_score * 0.15)

                    # 설정된 최종 임계값을 넘는 데이터만 결과에 포함
                    if final_score >= cfg['search']['score_threshold']:
                        res_item = {
                            "score": float(final_score), 
                            "source": cand.get('source')
                        }
                        
                        # 각 테이블 성격에 맞게 JSON 응답 포맷팅
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
                        
                        if len(res_item) > 1:
                            final_results.append(res_item)

                # 최종 산출된 가중합 점수 기준으로 내림차순 정렬
                final_results.sort(key=lambda x: x['score'], reverse=True)
                req_top_k = item['top_k'] if item['top_k'] else 5

                # 소요시간 및 로그 기록
                process_time = time.time() - start_time
                top_score = final_results[0]['score'] if final_results else 0.0
                short_q = queries[idx][:15].replace('\n', '') + "..."
                
                logger.info(f"[L3_IVFPQ][검색완료] 질의: '{short_q}' | 의도: {final_intents[idx]} | 최고점수: {top_score:.4f} | 소요시간: {process_time:.3f}s | 반환: {len(final_results[:req_top_k])}건")
                
                item['future'].set_result({"intent": final_intents[idx], "results": final_results[:req_top_k]})

        except Exception as e:
            print(f"[bold red]❌ L3 배치 처리 중 치명적 오류 발생: {e}[/bold red]")
            logger.error(f"L3 Batch Processing Error: {e}")
            for it in items:
                if not it['future'].done(): it['future'].set_exception(e)

# ==========================================
# 🚀 [4] FastAPI 라이프사이클 및 라우터 설정
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 구동 시 자원 로드 및 핵심 백그라운드 태스크 실행"""
    load_resources(full_load=True)
    processor_task = asyncio.create_task(batch_processor())
    watcher_task = asyncio.create_task(index_watcher())
    monitor_task = asyncio.create_task(resource_monitor())

    yield
    
    # 서버 종료 시 안전하게 취소
    processor_task.cancel()
    watcher_task.cancel()
    monitor_task.cancel()

app = FastAPI(title="GutJJeu RAG L3 Ultra-Optimized Server (IVF-PQ)", lifespan=lifespan)

@app.post("/search")
async def search(req: SearchRequest):
    """비동기 배치 처리를 위한 검색 요청 접수 엔드포인트"""
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    await search_queue.put({"q": req.q, "top_k": req.top_k, "age_group": req.age_group, "gender": req.gender, "future": future})
    return await future

@app.post("/sync-item/{source_id}")
async def trigger_item_sync(source_id: str):
    """지정된 ID의 데이터를 갱신하고 L6(Elasticsearch)로 전파합니다."""
    def notify_l6():
        try:
            import requests
            res = requests.post(f"http://localhost:8078/sync-item/{source_id}", timeout=5)
            print(f"✅ [L6 ES] 상위 노드 전파 성공: {source_id}")
        except Exception:
            pass
            
    threading.Thread(target=notify_l6, daemon=True).start()

    global _is_syncing
    if _is_syncing: return {"message": "이미 동기화 진행 중입니다."}
    
    def task():
        global _is_syncing
        _is_syncing = True
        try:
            import db_sync
            print(f"[magenta]🔄 '{source_id}' 단건 실시간 동기화를 시작합니다...[/magenta]")
            db_sync.run_sync(target_source_id=source_id)
            load_resources(full_load=False)
        finally: 
            _is_syncing = False
            
    threading.Thread(target=task).start()
    return {"message": f"'{source_id}' 통합 동기화 백그라운드 예약됨"}

if __name__ == "__main__":
    config = Config()
    config.bind = ["0.0.0.0:8077"]
    config.workers = 1 # 메모리 접근 충돌 방지를 위해 L3에서는 worker 1 권장
    config.alpn_protocols = ["h2", "http/1.1"]
    print("[bold red]🔥 GutJJeu RAG L3_IVFPQ Optimized Server 구동 완료! (Worker: 1)[/bold red]")
    asyncio.run(serve(app, config))