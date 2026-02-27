"""
[L6_Elasticsearch] FastAPI 비동기 임베딩 & 동기화 서버
Java Spring 백엔드에서 사용자 질문이 들어올 때 실시간으로 질문을 임베딩(Vector 변환)해주는 핵심 엔드포인트입니다.
APScheduler를 내장하여 매일 자정에 전체 DB를 자동 동기화하며, 단건 동기화 API도 제공합니다.
"""

import asyncio
import threading
import json
import yaml
import time
import pymysql
import requests
from pathlib import Path
from datetime import datetime
from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from loguru import logger
from rich import print
from contextlib import asynccontextmanager

# ==========================================
# ⚙️ [1] 환경 설정 및 전역 변수
# ==========================================
CONFIG_PATH = Path("../rag_config.yml")
if not CONFIG_PATH.exists():
    CONFIG_PATH = Path("rag_config.yml")

with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

# [비동기 로깅 설정] 
# enqueue=True 옵션을 통해 파일 쓰기(I/O)가 API 응답 속도를 깎아먹는 현상을 원천 차단
logger.add(
    "logs/rag_l6_{time:YYYY-MM-DD}.log", 
    rotation="100 MB", 
    enqueue=True,       
    format="{time:YYYY-MM-DD HH:mm:ss} | {level} | {message}"
)

# MariaDB 연결 설정 (config 누락 시 기본값 방어 로직 적용)
DB_CONFIG = {
    "host": cfg['database']['url'] or "localhost",
    "user": cfg['database']['username'] or "root",
    "password": cfg['database']['password'] or "",
    "database": cfg['database']['name'] or "GutJJeu",
    "port": cfg['database']['port'] or 3306,
    "charset": cfg['database']['charset'] or "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor
}

# AI 모델 및 Elasticsearch 대상 설정
MODEL_NAME = cfg['model']['embedding']
DEVICE = cfg['model']['device']
ES_BASE_URL = "http://52.79.170.61:30200" 
INDEX_NAME = "products-index"
VECTOR_FIELD = "product_vector"
ID_FIELD = "products_idx" 

# 전역 자원 및 멀티스레딩 락(Lock)
# 동시 요청 및 백그라운드 작업 간의 모델 접근 충돌을 방지합니다.
_model = None
_is_syncing = False
_model_lock = threading.Lock()
_sync_lock = threading.Lock()
scheduler = AsyncIOScheduler()

# API 요청 스키마
class QueryRequest(BaseModel):
    q: str

# ==========================================
# 📡 [2] DB 조회 및 데이터 가공 (ES 전송 규격화)
# ==========================================
def fetch_product_data(product_code: str = None) -> list:
    """상품 데이터를 DB에서 조회합니다. product_code가 있으면 단건 조회, 없으면 전체 조회입니다."""
    base_query = """
        SELECT 
            p.Products_IDX, p.Products_Code, p.Products_Name, p.Description, p.Price, p.Sale_Price, p.Status,
            c1.Category_Name AS Category_Name, c2.Category_Name AS IP_Name,
            GROUP_CONCAT(po.Option_Name SEPARATOR ', ') AS Options
        FROM Products p
        LEFT JOIN Products_Category c1 ON p.Products_Category = c1.Category_IDX
        LEFT JOIN Products_Category c2 ON p.Products_IP = c2.Category_IDX
        LEFT JOIN Product_Option po ON p.Products_IDX = po.Products_IDX AND po.Del = 0
        WHERE p.Del = 0 AND p.Status IN ('SALE', 'ON_SALE')
    """
    args = []
    if product_code:
        base_query += " AND p.Products_Code = %s"
        args.append(product_code)
    base_query += " GROUP BY p.Products_IDX"

    try:
        conn = pymysql.connect(**DB_CONFIG)
        with conn.cursor() as cursor:
            cursor.execute(base_query, args)
            rows = cursor.fetchall()
        conn.close()
        return rows
    except Exception as e:
        logger.error(f"[DB-Error] 상품 조회 실패: {e}")
        return []

def format_for_embedding(raw_data: list) -> list:
    """DB에서 조회된 원본 데이터를 임베딩용 텍스트와 고유 메타데이터 구조로 가공합니다."""
    formatted = []
    for row in raw_data:
        desc = row.get("Description", "") or "설명 없음"
        options = row.get("Options", "") or "단일 상품"
        
        # 검색 정확도를 높이기 위해 핵심 정보를 모두 담은 단일 텍스트 문자열 생성
        embed_text = (
            f"카테고리: {row['Category_Name']}, 캐릭터(IP): {row['IP_Name']}. "
            f"상품명: {row['Products_Name']}. 설명: {desc}. "
            f"판매 가격: {row['Sale_Price'] if row['Sale_Price'] > 0 else row['Price']}원. "
            f"옵션: {options}."
        )
        metadata = {
            "products_idx": str(row["Products_IDX"]),
            "products_code": row["Products_Code"]
        }
        formatted.append({"text": embed_text, "metadata": metadata})
    return formatted

# ==========================================
# 🚀 [3] Elasticsearch 전송 (Bulk Upsert)
# ==========================================
def send_to_es_bulk(ndjson_data: str):
    """NDJSON 형식의 페이로드를 Elasticsearch Bulk API로 HTTP POST 전송합니다."""
    url = f"{ES_BASE_URL}/_bulk"
    headers = {"Content-Type": "application/x-ndjson"}
    try:
        response = requests.post(url, data=ndjson_data, headers=headers, timeout=15)
        response.raise_for_status()
        return True
    except Exception as e:
        logger.error(f"[ES-Error] Bulk 업데이트 HTTP 전송 에러: {e}")
        return False

def safe_encode(texts: list, batch_size: int = 256):
    """다중 스레드 환경에서 모델 접근 충돌을 방지하기 위해 락(Lock)을 걸고 임베딩을 수행합니다."""
    global _model
    with _model_lock:
        return _model.encode(texts, normalize_embeddings=True, batch_size=batch_size).tolist()

def process_product_embeddings(data_list: list, batch_size: int = 256):
    """데이터 리스트를 받아 AI 벡터로 변환하고, ES에 Upsert 한 뒤 요약 로그를 남깁니다."""
    total_count = len(data_list)
    if total_count == 0: return
    
    start_time = time.time()
    total_success = 0

    for i in range(0, total_count, batch_size):
        batch = data_list[i : i + batch_size]
        texts = [item["text"] for item in batch]
        
        # GPU 임베딩 연산 수행
        vectors = safe_encode(texts, batch_size)
        
        # ES 규격(NDJSON)에 맞춘 페이로드 조립
        bulk_payload = ""
        for j, item in enumerate(batch):
            action = {"update": {"_index": INDEX_NAME, "_id": item["metadata"][ID_FIELD]}}
            doc = {"doc": {VECTOR_FIELD: vectors[j]}, "doc_as_upsert": True}
            bulk_payload += json.dumps(action) + "\n" + json.dumps(doc) + "\n"
            
        # 성공적으로 전송된 건수 누적
        if send_to_es_bulk(bulk_payload):
            total_success += len(batch)

    duration = time.time() - start_time
    logger.info(f"[L6_Elasticsearch][ES-Bulk] 업데이트 완료 | 성공건수: {total_success}/{total_count} | 소요시간: {duration:.2f}s")

# ==========================================
# ⏰ [4] 스케줄러(Cron) 및 서버 라이프사이클
# ==========================================
def run_full_sync_task(source: str):
    """전체 DB를 긁어와서 ES와 동기화하는 무거운 작업입니다. Lock을 이용해 중복 실행을 막습니다."""
    global _is_syncing
    with _sync_lock:
        if _is_syncing: return
        _is_syncing = True
        
    start_time = time.time()
    try:
        raw_data = fetch_product_data()
        formatted = format_for_embedding(raw_data)
        process_product_embeddings(formatted)
        duration = time.time() - start_time
        logger.info(f"[L6_Elasticsearch][Full-Sync] {source} 완료 | 총 처리량: {len(formatted)}건 | 소요시간: {duration:.2f}s")
    finally:
        with _sync_lock:
            _is_syncing = False

@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 기동 시 자원 초기화 및 백그라운드 스케줄러를 등록합니다."""
    global _model
    _model = SentenceTransformer(MODEL_NAME, device=DEVICE)
    print(f"✅ AI 언어 모델 로딩 완료: [green]{MODEL_NAME}[/green]")
    
    # [스케줄러 설정] 지연 실행(misfire) 허용 시간을 1시간으로 넉넉히 설정
    scheduler.configure(job_defaults={'misfire_grace_time': 3600})
    # 매일 자정(0시 0분)에 ES 전체 동기화 실행
    scheduler.add_job(run_full_sync_task, 'cron', hour=0, minute=0, args=["자정 자동 동기화"])
    scheduler.start()
    
    # 서버 기동 시 최초 1회 즉시 동기화 (데몬 스레드로 실행하여 서버 구동을 막지 않음)
    threading.Thread(target=run_full_sync_task, args=["서버 시작 초기화"], daemon=True).start()
    
    yield
    
    # 서버 종료 시 스케줄러 안전 해제
    scheduler.shutdown()

app = FastAPI(title="GutJJeu RAG L6 Elasticsearch Vector Server", lifespan=lifespan)

# ==========================================
# 🌐 [5] 외부 연동 API 엔드포인트
# ==========================================
@app.post("/embed-query")
async def embed_query(req: QueryRequest):
    """
    [핵심 API] 사용자 질문(텍스트)을 받아 AI 벡터 데이터(실수 배열)로 즉시 반환합니다.
    Java 서버에서 ES에 쿼리를 날리기 직전에 이 API를 호출합니다.
    """
    start_time = time.time()
    
    # 🎯 메인 이벤트 루프(API 응답 처리)가 블로킹(멈춤)되지 않도록 
    # 무거운 GPU 연산을 별도의 스레드 풀(to_thread)로 밀어냅니다.
    vectors = await asyncio.to_thread(safe_encode, [req.q])
    
    duration = time.time() - start_time
    # 🎯 성능 저하 없는 간결한 요약 로깅 (비동기 큐에 저장)
    logger.info(f"[L6_Elasticsearch][Embedding] 질의: '{req.q[:15]}...' | 소요시간: {duration:.3f}s | 상태: Success")
    
    return {"status": "success", "vector": vectors[0]}

@app.post("/sync-item/{product_code}")
async def sync_single_item(product_code: str, background_tasks: BackgroundTasks):
    """
    특정 상품의 데이터가 변경되었을 때, 해당 상품의 벡터만 ES에 재전송(Upsert)합니다.
    """
    def _sync_single_task(code: str):
        start_time = time.time()
        raw_data = fetch_product_data(product_code=code)
        
        # 상품이 삭제되었거나 조회되지 않는 경우 경고 로그
        if not raw_data:
            logger.warning(f"[L6_Elasticsearch][Sync-Item] 상품코드: {code} | 상태: Data Not Found (DB에서 찾을 수 없음)")
            return
            
        formatted = format_for_embedding(raw_data)
        process_product_embeddings(formatted)
        
        duration = time.time() - start_time
        logger.info(f"[L6_Elasticsearch][Sync-Item] 단건 동기화 완료 | 상품코드: {code} | 소요시간: {duration:.3f}s | 상태: OK")

    # API 응답은 즉시 반환하고, 실제 DB조회 및 ES 전송은 Background Task로 넘김
    background_tasks.add_task(_sync_single_task, product_code)
    return {"message": f"상품({product_code}) Elasticsearch 벡터 업데이트 작업이 백그라운드에 예약되었습니다."}

if __name__ == "__main__":
    import uvicorn
    # L6는 AI 모델 하나를 공유하며 병렬 스레딩으로 처리하므로, GPU 메모리 경합 방지를 위해 worker=1 로 고정합니다.
    uvicorn.run(app, host="0.0.0.0", port=8078, workers=1)