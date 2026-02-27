"""
[L4_Qdrant] FastAPI 비동기 RAG 검색 서버
Qdrant 벡터 DB의 네이티브 메타데이터 필터링(Native Payload Filtering) 기능을 사용하여
사용자 조건에 맞는 데이터를 DB 단에서 초고속으로 먼저 걸러낸 뒤,
정확하게 교정된 실무형 스마트 가중합 로직을 통해 최종 순위를 산출합니다.
"""

import threading
import json
import yaml
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
from qdrant_client import QdrantClient
from qdrant_client.http import models as qmodels

# ==========================================
# ⚙️ [1] 환경 설정 및 전역 자원 초기화
# ==========================================
CONFIG_PATH = Path("../rag_config.yml")
with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

CATEGORIES_PATH = Path("../data/categories.json")
INTENT_CATEGORIES = {}
if CATEGORIES_PATH.exists():
    with open(CATEGORIES_PATH, "r", encoding="utf-8") as f:
        INTENT_CATEGORIES = json.load(f)

# [Loguru 로깅 설정] L4 전용 로그 파일명 지정
logger.add(
    "logs/rag_l4_server_{time:YYYY-MM-DD}.log", 
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

# Qdrant 클라이언트 설정
client = QdrantClient(host=cfg['vector_db']['qdrant']['host'], port=cfg['vector_db']['qdrant']['port'])
COLLECTION_NAME = cfg['vector_db']['qdrant']['collection']

# AI 모델 및 큐
_model, _reranker = None, None
_is_syncing = False
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
    """서버 하드웨어 사용량 반환"""
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
    """매초 시스템 자원 로깅"""
    net_init = psutil.net_io_counters()
    last_sent = net_init.bytes_sent
    last_recv = net_init.bytes_recv
    model_name = cfg['model']['embedding'].split("/")[-1]

    while True:
        try:
            cpu, ram, gpu, mb_s, mb_r, last_sent, last_recv = await asyncio.to_thread(
                fetch_hardware_status, last_sent, last_recv
            )
            logger.info(f"[L4_Qdrant][자원] CPU: {cpu:04.1f}% | RAM: {ram:04.1f}% | GPU: {gpu} | NET: ↑{mb_s:.2f}MB/s ↓{mb_r:.2f}MB/s | Model: {model_name}")
        except Exception as e:
            logger.error(f"자원 모니터링 오류: {e}")
        await asyncio.sleep(1.0)

# ==========================================
# 📡 [2] 리소스 로드 
# ==========================================
def load_resources():
    """AI 언어 모델(임베딩, 리랭커)을 GPU/CPU 메모리에 적재합니다."""
    global _model, _reranker
    if _model is None:
        print(f"[bold blue][{datetime.now().strftime('%H:%M:%S')}] 🤖 L4 Qdrant AI 모델 로딩 시작...[/bold blue]")
        _model = SentenceTransformer(cfg['model']['embedding'], device=cfg['model']['device'])
        _reranker = CrossEncoder(cfg['model']['reranker'], device=cfg['model']['device'])
        print(f"[bold green][{datetime.now().strftime('%H:%M:%S')}] ✅ L4 리소스 적재 완료[/bold green]")

# ==========================================
# 🧪 [3] 극한 최적화 배치 엔진 (Qdrant 통합)
# ==========================================
async def batch_processor():
    """
    Qdrant의 Payload 필터링을 사용하여 예선을 통과한 후보들을 가져온 뒤,
    오류가 제거된 85%:15% 스마트 가중합 로직을 통해 최종 결과를 산출합니다.
    """
    while True:
        first_item = await search_queue.get()
        items = [first_item]
        start_time = time.time()
        
        # 병렬 처리를 위해 큐 수집
        while len(items) < 16 and (time.time() - start_time) < 0.1:
            try:
                items.append(search_queue.get_nowait())
            except asyncio.QueueEmpty:
                await asyncio.sleep(0.01)

        queries = [it['q'] for it in items]
        
        try:
            # ----------------------------------------------------
            # [STEP 1] 의도 분류 
            # ----------------------------------------------------
            final_intents = ["GENERAL_CHAT"] * len(items)
            if INTENT_CATEGORIES:
                intent_keys = list(INTENT_CATEGORIES.keys())
                intent_values = list(INTENT_CATEGORIES.values())
                intent_pairs = [[q, v] for q in queries for v in intent_values]
                intent_scores = _reranker.predict(intent_pairs, batch_size=len(intent_pairs))
                for i in range(len(items)):
                    s_idx = i * len(intent_values)
                    e_idx = s_idx + len(intent_values)
                    final_intents[i] = intent_keys[np.argmax(intent_scores[s_idx:e_idx])]

            # ----------------------------------------------------
            # [STEP 2] 임베딩 (질문 벡터화)
            # ----------------------------------------------------
            q_vecs = _model.encode(queries, normalize_embeddings=True, batch_size=len(queries)).astype("float32")
            
            # ----------------------------------------------------
            # [STEP 3] Qdrant 메타데이터 필터링 (Native Filtering) 및 검색
            # ----------------------------------------------------
            all_candidates_per_item = []
            rerank_pairs = []
            item_candidate_counts = []

            for idx, item in enumerate(items):
                req_age = item.get('age_group')
                req_gender = item.get('gender')
                req_top_k = item.get('top_k') if item.get('top_k') else 5
                fetch_limit = req_top_k * 5
                
                # ⭐ Qdrant Native Filter 생성 (성능 최적화의 핵심)
                # 데이터베이스 단에서 나이/성별이 내 정보와 일치하거나 '무관'인 것만 가져오도록 명령
                must_conds = []
                if req_age:
                    must_conds.append(qmodels.FieldCondition(key="target_age", match=qmodels.MatchAny(any=[req_age, "무관"])))
                if req_gender:
                    must_conds.append(qmodels.FieldCondition(key="target_gender", match=qmodels.MatchAny(any=[req_gender, "무관"])))
                
                q_filter = qmodels.Filter(must=must_conds) if must_conds else None

                try:
                    # Qdrant 서버로 쿼리 전송 (비동기 I/O 위임)
                    search_result = await asyncio.to_thread(
                        client.search,
                        collection_name=COLLECTION_NAME,
                        query_vector=q_vecs[idx].tolist(),
                        query_filter=q_filter,
                        limit=fetch_limit,
                        with_payload=True # Payload(메타데이터) 동시 수신
                    )
                    candidates = [res.payload for res in search_result]
                except Exception as e:
                    logger.error(f"[L4_Qdrant] Search error: {e}")
                    candidates = []

                all_candidates_per_item.append(candidates)
                item_candidate_counts.append(len(candidates))
                
                for cand in candidates:
                    rerank_pairs.append([queries[idx], cand['text']])

            # ----------------------------------------------------
            # [STEP 4] 리랭킹 거대 배치 처리 (정밀 교정)
            # ----------------------------------------------------
            all_scores = []
            if rerank_pairs:
                all_scores = _reranker.predict(rerank_pairs, batch_size=len(rerank_pairs))

            # ----------------------------------------------------
            # [STEP 5] ⭐ 완벽하게 수정된 실무형 데이터 유형별 점수 산출
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
                    # ⭐ [핵심 버그 수정 적용] Sigmoid 완전 제거, 순수 모델 확률 점수 사용!
                    semantic_prob = float(item_scores[i])
                    if semantic_prob < 0: semantic_prob = 0.0 # 극단적 음수 방어
                    
                    source_table = cand.get('source', '').split(":")[0]
                    target_score = 0.0

                    # 1. 오직 "상품(Products)"일 경우에만 타겟 가중치를 계산
                    if source_table == "Products":
                        cand_age = cand.get('target_age', ["무관"])
                        cand_gen = cand.get('target_gender', ["무관"])
                        # 조건당 0.5점씩 가산
                        if req_age and req_age in cand_age: target_score += 0.5
                        if req_gender and req_gender in cand_gen: target_score += 0.5

                    # 2. 공지/QnA이거나, 상품이더라도 연관성이 30점(0.3) 미만이면 가중치 배제 (의미 역전 방지)
                    if source_table != "Products" or semantic_prob < 0.3:
                        final_score = semantic_prob
                    else:
                        # 질문과 연관성이 입증된 상품에 한해 가중합(의미 85% : 개인화 15%) 적용
                        final_score = (semantic_prob * 0.85) + (target_score * 0.15)

                    # 설정된 최종 컷오프 임계값을 넘는 데이터만 결과 목록에 삽입
                    if final_score >= cfg['search']['score_threshold']:
                        res_item = {
                            "score": float(final_score), 
                            "source": cand.get('source')
                        }
                        
                        # 테이블별 일관된 JSON 스키마 포맷팅
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

                # 최종 가중합 점수 기준으로 내림차순 정렬
                final_results.sort(key=lambda x: x['score'], reverse=True)
                req_top_k = item['top_k'] if item['top_k'] else 5

                # 소요시간 및 로깅
                process_time = time.time() - start_time
                top_score = final_results[0]['score'] if final_results else 0.0
                short_q = queries[idx][:15].replace('\n', '') + "..."
                
                logger.info(f"[L4_Qdrant][검색완료] 질의: '{short_q}' | 의도: {final_intents[idx]} | 최고점수: {top_score:.4f} | 소요시간: {process_time:.3f}s | 반환: {len(final_results[:req_top_k])}건")
                
                # 비동기 Future 응답 셋팅
                item['future'].set_result({"intent": final_intents[idx], "results": final_results[:req_top_k]})

        except Exception as e:
            print(f"[bold red]❌ L4 배치 처리 중 오류 발생: {e}[/bold red]")
            logger.error(f"L4 Batch Processing Error: {e}")
            for it in items:
                if not it['future'].done(): it['future'].set_exception(e)

# ==========================================
# 🚀 [4] FastAPI 라이프사이클 및 엔드포인트
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 구동 시 Qdrant 연결 및 백그라운드 태스크를 초기화합니다."""
    load_resources()
    processor_task = asyncio.create_task(batch_processor())
    monitor_task = asyncio.create_task(resource_monitor())

    yield
    
    # 종료 시 자원 안전 해제
    processor_task.cancel()
    monitor_task.cancel()

app = FastAPI(title="GutJJeu RAG L4 Qdrant Server", lifespan=lifespan)

@app.post("/search")
async def search(req: SearchRequest):
    """Qdrant 검색 요청을 접수합니다."""
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    await search_queue.put({"q": req.q, "top_k": req.top_k, "age_group": req.age_group, "gender": req.gender, "future": future})
    return await future

@app.post("/sync-item/{source_id}")
async def trigger_item_sync(source_id: str):
    """특정 데이터 변경을 감지하고 Qdrant와 상위 노드(Elasticsearch)로 갱신을 전파합니다."""
    def notify_l6():
        try:
            import requests
            res = requests.post(f"http://localhost:8078/sync-item/{source_id}", timeout=5)
            print(f"✅ [L6 ES] 상위 노드 전파 성공: {source_id}")
        except Exception as e:
            print(f"[yellow]⚠️ [L6 ES] 상위 노드 전파 실패: {e}[/yellow]")
            
    threading.Thread(target=notify_l6, daemon=True).start()

    global _is_syncing
    if _is_syncing: return {"message": "이미 Qdrant 동기화가 진행 중입니다."}
    
    def task():
        global _is_syncing
        _is_syncing = True
        try:
            import db_sync
            print(f"[magenta]🔄 '{source_id}' 데이터의 실시간 Qdrant 동기화를 시작합니다...[/magenta]")
            db_sync.run_sync(target_source_id=source_id)
        finally: 
            _is_syncing = False
            
    threading.Thread(target=task).start()
    return {"message": f"'{source_id}' Qdrant 단건 동기화 예약됨"}

if __name__ == "__main__":
    config = Config()
    config.bind = ["0.0.0.0:8077"]
    config.workers = 1 # Qdrant 클라이언트의 네트워크 리소스 분배를 위해 워커 1 추천
    config.alpn_protocols = ["h2", "http/1.1"]
    print("[bold red]🔥 GutJJeu RAG L4_Qdrant Optimized Server 구동 완료! (Worker: 1)[/bold red]")
    asyncio.run(serve(app, config))