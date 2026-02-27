"""
[L5_Milvus] FastAPI 비동기 RAG 검색 서버
Milvus의 JSON Expr(Expression) 필터링을 사용하여 엄청나게 빠른 속도로 1차 예선(Pre-filtering)을 거친 후,
정밀하게 교정된 실무형 스마트 가중합 로직을 통해 최종 순위를 산출하는 상용 등급 검색 엔진입니다.
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
from pymilvus import connections, Collection

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

# [Loguru 로깅 설정] L5 전용 로그 파일명 지정
logger.add(
    "logs/rag_l5_server_{time:YYYY-MM-DD}.log", 
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

# Milvus 클라이언트 설정
MILVUS_HOST = cfg['vector_db']['milvus']['host']
MILVUS_PORT = cfg['vector_db']['milvus']['port']
COLLECTION_NAME = cfg['vector_db']['milvus']['collection']

# AI 모델 및 비동기 큐
_model, _reranker = None, None
_collection = None
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
    """매초 시스템 자원 백그라운드 로깅"""
    net_init = psutil.net_io_counters()
    last_sent = net_init.bytes_sent
    last_recv = net_init.bytes_recv
    model_name = cfg['model']['embedding'].split("/")[-1]

    while True:
        try:
            cpu, ram, gpu, mb_s, mb_r, last_sent, last_recv = await asyncio.to_thread(
                fetch_hardware_status, last_sent, last_recv
            )
            logger.info(f"[L5_Milvus][자원] CPU: {cpu:04.1f}% | RAM: {ram:04.1f}% | GPU: {gpu} | NET: ↑{mb_s:.2f}MB/s ↓{mb_r:.2f}MB/s | Model: {model_name}")
        except Exception as e:
            logger.error(f"자원 모니터링 오류: {e}")
        await asyncio.sleep(1.0)

# ==========================================
# 📡 [2] 리소스 및 Milvus 컬렉션 로드
# ==========================================
def load_resources():
    """AI 모델을 적재하고 Milvus 컬렉션을 메모리에 로드(Load)하여 검색 대기 상태로 만듭니다."""
    global _model, _reranker, _collection
    if _model is None:
        print(f"[bold blue][{datetime.now().strftime('%H:%M:%S')}] 🤖 L5 Milvus AI 모델 로딩 시작...[/bold blue]")
        _model = SentenceTransformer(cfg['model']['embedding'], device=cfg['model']['device'])
        _reranker = CrossEncoder(cfg['model']['reranker'], device=cfg['model']['device'])
        
        # Milvus 서버 연결 및 메모리 로드 (Milvus는 컬렉션을 메모리에 로드해야 검색 가능)
        connections.connect("default", host=MILVUS_HOST, port=MILVUS_PORT)
        _collection = Collection(COLLECTION_NAME)
        _collection.load()
        print(f"[bold green][{datetime.now().strftime('%H:%M:%S')}] ✅ L5 Milvus 컬렉션 및 리소스 로드 완료[/bold green]")

# ==========================================
# 🧪 [3] 극한 최적화 배치 엔진 (Milvus 통합)
# ==========================================
async def batch_processor():
    """
    Milvus의 JSON 메타데이터 필터링(Expr)을 수행한 뒤,
    오류가 제거된 85%:15% 스마트 가중합 로직을 통해 최종 결과를 산출합니다.
    """
    while True:
        first_item = await search_queue.get()
        items = [first_item]
        start_time = time.time()
        
        # 비동기 병렬 처리를 위한 요청 Gather
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
            # [STEP 3] Milvus JSON Expression(Expr) 필터링 및 검색
            # ----------------------------------------------------
            all_candidates_per_item = []
            rerank_pairs = []
            item_candidate_counts = []

            for idx, item in enumerate(items):
                req_age = item.get('age_group')
                req_gender = item.get('gender')
                req_top_k = item.get('top_k') if item.get('top_k') else 5
                fetch_limit = req_top_k * 5
                
                # ⭐ Milvus Native JSON Expression 쿼리 문자열 조립
                # metadata 필드 내의 JSON 키를 직접 타겟팅하여 초고속 필터링을 수행합니다.
                exprs = []
                if req_age:
                    exprs.append(f"JSON_CONTAINS_ANY(metadata['target_age'], ['{req_age}', '무관'])")
                if req_gender:
                    exprs.append(f"JSON_CONTAINS_ANY(metadata['target_gender'], ['{req_gender}', '무관'])")
                expr = " and ".join(exprs) if exprs else None

                try:
                    # Milvus 서버로 검색 명령 전송
                    # ef 파라미터 제어를 통해 HNSW 탐색 정확도를 높임
                    search_params = {"metric_type": "IP", "params": {"ef": 64}}
                    search_result = await asyncio.to_thread(
                        _collection.search,
                        data=q_vecs[idx:idx+1].tolist(),
                        anns_field="vector",
                        param=search_params,
                        limit=fetch_limit,
                        expr=expr,
                        output_fields=["source", "metadata"] # 반환받을 필드 지정
                    )
                    
                    # Milvus SearchResult 객체 파싱
                    candidates = []
                    if search_result:
                        for hit in search_result[0]:
                            meta = hit.entity.get("metadata")
                            # 혹시 source가 JSON 메타에 누락되었을 경우를 대비한 보정
                            if "source" not in meta:
                                meta["source"] = hit.entity.get("source")
                            candidates.append(meta)
                except Exception as e:
                    logger.error(f"[L5_Milvus] Search error: {e}")
                    candidates = []

                all_candidates_per_item.append(candidates)
                item_candidate_counts.append(len(candidates))
                
                for cand in candidates:
                    # 메타데이터 내부의 text(원본) 추출하여 리랭킹 쌍 생성
                    rerank_pairs.append([queries[idx], cand.get('text', '')])

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
                    # ⭐ [버그 수정 적용] Sigmoid 제거! 순수 모델 확률 점수 사용
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

                    # 2. 공지/QnA이거나, 상품이더라도 연관성이 30점 미만이면 가중치 배제 (의미 역전 완전 차단)
                    if source_table != "Products" or semantic_prob < 0.3:
                        final_score = semantic_prob
                    else:
                        # 통과된 관련 상품에 한해 가중합(의미 85% : 개인화 15%) 적용
                        final_score = (semantic_prob * 0.85) + (target_score * 0.15)

                    # 설정된 최종 임계값을 넘는 데이터만 결과 목록에 편입
                    if final_score >= cfg['search']['score_threshold']:
                        res_item = {
                            "score": float(final_score), 
                            "source": cand.get('source')
                        }
                        
                        # 테이블별 일관된 JSON 포맷팅 조립
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
                
                logger.info(f"[L5_Milvus][검색완료] 질의: '{short_q}' | 의도: {final_intents[idx]} | 최고점수: {top_score:.4f} | 소요시간: {process_time:.3f}s | 반환: {len(final_results[:req_top_k])}건")
                
                item['future'].set_result({"intent": final_intents[idx], "results": final_results[:req_top_k]})

        except Exception as e:
            print(f"[bold red]❌ L5 배치 처리 중 치명적 오류 발생: {e}[/bold red]")
            logger.error(f"L5 Batch Processing Error: {e}")
            for it in items:
                if not it['future'].done(): it['future'].set_exception(e)

# ==========================================
# 🚀 [4] FastAPI 라이프사이클 및 엔드포인트
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 구동 시 Milvus 연결 및 백그라운드 태스크를 초기화합니다."""
    load_resources()
    processor_task = asyncio.create_task(batch_processor())
    monitor_task = asyncio.create_task(resource_monitor())

    yield
    
    processor_task.cancel()
    monitor_task.cancel()

app = FastAPI(title="GutJJeu RAG L5 Milvus Server", lifespan=lifespan)

@app.post("/search")
async def search(req: SearchRequest):
    """Milvus 검색 요청 접수 엔드포인트"""
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    await search_queue.put({"q": req.q, "top_k": req.top_k, "age_group": req.age_group, "gender": req.gender, "future": future})
    return await future

@app.post("/sync-item/{source_id}")
async def trigger_item_sync(source_id: str):
    """특정 데이터 변경을 감지하고 Milvus 컬렉션과 상위 노드(Elasticsearch)로 갱신을 전파합니다."""
    def notify_l6():
        try:
            import requests
            res = requests.post(f"http://localhost:8078/sync-item/{source_id}", timeout=5)
            print(f"✅ [L6 ES] 상위 노드 전파 성공: {source_id}")
        except Exception:
            pass
            
    threading.Thread(target=notify_l6, daemon=True).start()

    global _is_syncing
    if _is_syncing: return {"message": "이미 Milvus 동기화가 진행 중입니다."}
    
    def task():
        global _is_syncing
        _is_syncing = True
        try:
            import db_sync
            print(f"[magenta]🔄 '{source_id}' 데이터의 실시간 Milvus 단건 동기화를 시작합니다...[/magenta]")
            db_sync.run_sync(target_source_id=source_id)
        finally: 
            _is_syncing = False
            
    threading.Thread(target=task).start()
    return {"message": f"'{source_id}' Milvus 단건 동기화 예약됨"}

if __name__ == "__main__":
    config = Config()
    config.bind = ["0.0.0.0:8077"]
    config.workers = 1 # Milvus 클라이언트 I/O 효율을 위해 워커는 1로 권장
    config.alpn_protocols = ["h2", "http/1.1"]
    print("[bold red]🔥 GutJJeu RAG L5_Milvus Optimized Server 구동 완료! (Worker: 1)[/bold red]")
    asyncio.run(serve(app, config))