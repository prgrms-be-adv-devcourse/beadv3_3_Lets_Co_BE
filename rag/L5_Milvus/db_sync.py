"""
[L5_Milvus] 데이터베이스 동기화 및 벡터 DB 업서트 스크립트
MariaDB의 데이터를 추출하여 타겟 정보를 분석한 뒤,
초대규모 데이터 처리에 특화된 Milvus 벡터 데이터베이스에 저장합니다.
메타데이터를 JSON 형식의 필드로 묶어 저장함으로써 빠르고 유연한 사전 필터링을 지원합니다.
"""

import json
import yaml
import pymysql
import re
import base64
from pathlib import Path
from datetime import datetime
from sentence_transformers import SentenceTransformer
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection, utility
from Crypto.Cipher import AES
from rich import print

# ==========================================
# ⚙️ [1] 환경 설정 및 매핑 데이터 로드
# ==========================================
CONFIG_PATH = Path("../rag_config.yml")
TABLE_MAP_PATH = Path("../data/table_map.json")

if not CONFIG_PATH.exists():
    raise FileNotFoundError("rag_config.yml 파일을 찾을 수 없습니다.")
if not TABLE_MAP_PATH.exists():
    raise FileNotFoundError("data/table_map.json 파일을 찾을 수 없습니다.")

with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

with open(TABLE_MAP_PATH, "r", encoding="utf-8") as f:
    TABLE_MAP = json.load(f)

# Milvus 서버 연결 정보 및 모델 설정
MILVUS_HOST = cfg['vector_db']['milvus']['host']
MILVUS_PORT = cfg['vector_db']['milvus']['port']
COLLECTION_NAME = cfg['vector_db']['milvus']['collection']
STATE_FILE = Path("./sync_state.json") # 증분 동기화 기준 시간 저장용 파일
MODEL_NAME = cfg['model']['embedding']
DEVICE = cfg['model']['device']

# ==========================================
# 🛠️ [2] 유틸리티 함수 (복호화 및 연령 계산)
# ==========================================
def decrypt_aes_gcm(encrypted_base64, secret_key_str):
    """
    [보안] Java Spring 서버와 동일한 AES-GCM (256비트) 방식으로 고객 생년월일 등을 복호화합니다.
    """
    if not encrypted_base64:
        return None
    try:
        raw_key = secret_key_str.encode('utf-8')
        key_bytes = bytearray(32)
        length = min(len(raw_key), 32)
        key_bytes[:length] = raw_key[:length]
        key = bytes(key_bytes)

        combined = base64.b64decode(encrypted_base64)
        iv = combined[:12]
        ciphertext = combined[12:-16]
        tag = combined[-16:]

        cipher = AES.new(key, AES.MODE_GCM, nonce=iv)
        plaintext = cipher.decrypt_and_verify(ciphertext, tag)
        return plaintext.decode('utf-8')
    except Exception as e:
        print(f"[bold red]⚠️ [복호화 오류]: {e}[/bold red]")
        return None

def get_age_group(birth_str):
    """생년월일(예: 1985-11-20)에서 현재 연도 기준의 10단위 연령대(예: 40대)를 산출합니다."""
    if not birth_str:
        return "연령 미상"
    try:
        num_str = re.sub(r'[^0-9]', '', str(birth_str))
        if len(num_str) < 4:
            return "연령 미상"
        birth_year = int(num_str[:4])
        current_year = datetime.now().year
        age = current_year - birth_year
        return f"{(age // 10) * 10}대"
    except Exception:
        return "연령 미상"

# ==========================================
# 📦 [3] Milvus 스키마 정의 및 연결
# ==========================================
def connect_milvus():
    """Milvus 서버와의 TCP 연결을 수립합니다."""
    connections.connect("default", host=MILVUS_HOST, port=MILVUS_PORT)

def init_milvus_collection():
    """
    Milvus 컬렉션 스키마를 정의하고 생성합니다.
    ⭐ [핵심] JSON DataType을 사용하여 메타데이터를 유연하게 담을 수 있도록 설계되었습니다.
    """
    connect_milvus()
    
    # 이미 컬렉션이 존재하면 그대로 반환
    if utility.has_collection(COLLECTION_NAME):
        return Collection(COLLECTION_NAME)

    print(f"[yellow]⚠️ Milvus 컬렉션({COLLECTION_NAME})이 없어 새로 생성합니다.[/yellow]")
    
    # 스키마(테이블 구조) 정의
    fields = [
        FieldSchema(name="source", dtype=DataType.VARCHAR, is_primary=True, max_length=500), # 기본키
        FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR, dim=1024),                   # 임베딩 벡터
        FieldSchema(name="metadata", dtype=DataType.JSON)                                    # JSON 자유 필드
    ]
    schema = CollectionSchema(fields, "GutJJeu L5 Enterprise Knowledge Base")
    collection = Collection(COLLECTION_NAME, schema)

    # 내적(IP) 기반의 HNSW 인덱스 생성
    index_params = {
        "metric_type": "IP",
        "index_type": "HNSW",
        "params": {"M": 16, "efConstruction": 40}
    }
    collection.create_index(field_name="vector", index_params=index_params)
    print("[green]✅ Milvus 컬렉션 및 HNSW 인덱스 생성 완료.[/green]")
    
    return collection

def get_db_conn():
    """MariaDB 연결 객체를 생성하여 반환합니다."""
    db_cfg = cfg['database']
    return pymysql.connect(
        host=db_cfg['url'],
        user=db_cfg['username'],
        password=db_cfg['password'],
        database=db_cfg['name'],
        port=db_cfg['port'],
        charset=db_cfg['charset'],
        cursorclass=pymysql.cursors.DictCursor
    )

# ==========================================
# 📡 [4] DB 데이터 추출 및 전처리 로직
# ==========================================
def fetch_changed_data(last_time=None, target_source_id=None):
    """
    MariaDB에서 데이터를 추출하고, 타겟(연령/성별)을 상품 데이터에 전파하여 
    Milvus의 JSON 필드 규격에 맞는 metadata 딕셔너리로 만듭니다.
    """
    conn = get_db_conn()
    changes = []
    aes_secret_key = cfg.get('security', {}).get('aes256_key', 'YOUR_SECRET_KEY_HERE_EXACTLY_SAME')
    
    print(f"[cyan]데이터베이스(MariaDB) 증분 조회 시작... (L5_Milvus 대상)[/cyan]")
    if last_time:
        print(f"기준 시간 (Updated_at > {last_time}) 이후 변경된 데이터만 가져옵니다.")

    try:
        with conn.cursor() as cursor:
            # ----------------------------------------------------
            # [A] 상품별 타겟 분석 (실제 주문 내역 조인)
            # ----------------------------------------------------
            product_demographics = {}
            cursor.execute("""
                SELECT p.Products_IDX, ui.Birth AS birth, ui.Gender AS gender
                FROM Orders o
                JOIN Orders_Item oi ON o.Orders_IDX = oi.Orders_IDX
                JOIN Products p ON oi.Products_Name = p.Products_Name
                JOIN Users_Information ui ON o.Users_IDX = ui.Users_IDX
                WHERE o.Del = 0 AND p.Del = 0 AND ui.Del != 1
                AND o.Created_at >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            """)
            for row in cursor.fetchall():
                p_idx = row['Products_IDX']
                gender = row['gender']
                birth_enc = row['birth']
                
                birth_dec = decrypt_aes_gcm(birth_enc, aes_secret_key)
                
                # 복호화 체크용 디버그 출력 (필요 시 주석 해제)
                # if birth_dec: print(f"복호화 성공: {birth_dec}")

                age_group = get_age_group(birth_dec)
                
                if p_idx not in product_demographics:
                    product_demographics[p_idx] = {} # 구조 변경: {(성별, 연령): 카운트}
                
                if gender and age_group != "연령 미상":
                    pair_key = (gender, age_group)
                    product_demographics[p_idx][pair_key] = product_demographics[p_idx].get(pair_key, 0) + 1

            # ----------------------------------------------------
            # [B] 부가 데이터 사전 로드 (옵션/QnA)
            # ----------------------------------------------------
            cursor.execute("SELECT * FROM Product_Option WHERE Status IN ('SALE', 'ON_SALE') AND Del = 0")
            opt_dict = {}
            for opt in cursor.fetchall():
                p_idx = opt['Products_IDX']
                if p_idx not in opt_dict: opt_dict[p_idx] = []
                opt_dict[p_idx].append({
                    "Option_Name": opt['Option_Name'],
                    "Option_Price": float(opt['Option_Price']),
                    "Option_Sale_Price": float(opt['Option_Sale_Price']) if opt['Option_Sale_Price'] > 0 else ""
                })

            cursor.execute("SELECT Customer_Service_IDX, Content FROM Customer_Service_Detail WHERE Parent_IDX IS NOT NULL AND Del = 0")
            ans_dict = {row['Customer_Service_IDX']: row['Content'] for row in cursor.fetchall()}
            cursor.execute("SELECT Customer_Service_IDX, Content FROM Customer_Service_Detail WHERE Parent_IDX IS NULL AND Del = 0")
            q_dict = {row['Customer_Service_IDX']: row['Content'] for row in cursor.fetchall()}

            # ----------------------------------------------------
            # [C] 테이블 매핑 순회 및 증분 데이터 병합
            # ----------------------------------------------------
            for table, info in TABLE_MAP.items():
                if table == "Customer_Service_Detail": continue
                
                base_sql = info.get("query")
                # 삭제(Del=1) 이력을 추적하여 Milvus에서 지우기 위해 Del=0 하드 조건을 임시 제거
                base_sql = re.sub(r'(?i)\bAND\s+(cs\.)?Del\s*=\s*0\b', '', base_sql)
                
                params = []
                where_clauses = []
                
                if target_source_id:
                    t_name, t_idx = target_source_id.split(":")
                    if t_name != table: continue
                    where_clauses.append(f"{info['id']} = %s")
                    params.append(t_idx)
                elif last_time:
                    time_col = "cs.Updated_at" if table == "Customer_Service" else "Updated_at"
                    where_clauses.append(f"{time_col} > %s")
                    params.append(last_time)
                
                if where_clauses:
                    connector = " AND " if "WHERE" in base_sql.upper() else " WHERE "
                    base_sql += connector + " AND ".join(where_clauses)
                
                cursor.execute(base_sql, params)
                rows = cursor.fetchall()
                
                for r in rows:
                    combined_text = " ".join([str(r[col.split('.')[-1]]) for col in info['text_cols'] if r.get(col.split('.')[-1])])
                    source_id = f"{table}:{r[info['id'].split('.')[-1]]}"
                    
                    # ⭐ [버그 패치] 오직 'Products'일 때만 나이/성별 부여. 공지는 무조건 무관.
                    target_gender, target_age = ["무관"], ["무관"]  # 이제 리스트 형태로 저장됩니다!
                    # 테이블 순회 로직 내부의 Products 처리 부분
                    if table == "Products":
                        p_idx = r.get('Products_IDX')
                        target_gender, target_age = ["무관"], ["무관"]
                        
                        if p_idx and p_idx in product_demographics:
                            demo = product_demographics[p_idx]
                            
                            valid_genders = set()
                            valid_ages = set()
                            
                            # (성별, 연령) 조합이 10건 이상인 것만 추출
                            for (g, a), count in demo.items():
                                if count >= 10:
                                    valid_genders.add(g)
                                    valid_ages.add(a)
                            
                            if valid_genders: target_gender = list(valid_genders)
                            if valid_ages: target_age = list(valid_ages)

                    # Milvus JSON 필드(metadata)에 그대로 주입될 딕셔너리 구성
                    metadata = {
                        "source": source_id,
                        "text": combined_text.strip(),
                        "is_deleted": (r.get('Del', 0) != 0),
                        "target_gender": target_gender,
                        "target_age": target_age
                    }

                    if table == "Products":
                        metadata.update({
                            "Products_Code": r['Products_Code'],
                            "Products_Name": r['Products_Name'],
                            "Description": r['Description'],
                            "Category_Name": r['Category_Name'],
                            "IP_Name": r['IP_Name'],
                            "Price": float(r['Price']),
                            "Sale_Price": float(r['Sale_Price']) if r['Sale_Price'] > 0 else "",
                            "View_Count": r['View_Count'],
                            "Order_Count": int(r.get('Order_Count', 0)),
                            "Review_Count": int(r.get('Review_Count', 0)),
                            "Options": opt_dict.get(r['Products_IDX'], [])
                        })
                    elif table == "Customer_Service":
                        cs_idx = r['Customer_Service_IDX']
                        metadata.update({
                            "Customer_Service_Code": r['Customer_Service_Code'],
                            "Type": r['Type'],
                            "Category": r['Category'],
                            "Status": r['Status'],
                            "Title": r['Title'],
                            "View_Count": r.get('View_Count', 0),
                            "Is_Pinned": r.get('Is_Pinned', 0),
                            "Published_at": str(r.get('Published_at', '')),
                            "Created_at": str(r.get('Created_at', '')),
                            "Question": q_dict.get(cs_idx, r['Title']),
                            "Answer": ans_dict.get(cs_idx, ""),
                            "Products_Name": r.get('Products_Name', '')
                        })
                    changes.append(metadata)
    finally:
        conn.close()
        
    print(f"[cyan]데이터 추출 완료! (대상 데이터 수: {len(changes)}건)[/cyan]")
    return changes

# ==========================================
# 🚀 [5] Milvus 컬렉션 갱신 및 데이터 업서트
# ==========================================
def run_sync(target_source_id=None):
    """
    MariaDB 변경 사항을 확인하여 삭제된 데이터는 Milvus에서 제거하고,
    추가/변경된 데이터는 벡터 임베딩 후 Milvus 컬렉션에 Upsert 합니다.
    """
    collection = init_milvus_collection()
    print(f"[bold magenta][{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] L5_Milvus 동기화 가동 (타임스탬프 증분 모드)[/bold magenta]")
    
    # 마지막 동기화 시간 로드
    last_run = "1970-01-01 00:00:00"
    if STATE_FILE.exists():
        last_run = json.loads(STATE_FILE.read_text()).get("last_run", last_run)
    
    current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    changed_items = fetch_changed_data(last_run if not target_source_id else None, target_source_id)

    if not changed_items and not target_source_id:
        print("[yellow]DB 변경 사항이 없습니다. 동기화를 종료합니다.[/yellow]")
        return
    elif not changed_items and target_source_id:
        changed_items = [{"source": target_source_id, "is_deleted": True}]

    # 로컬에서 임베딩 수행 (Milvus는 클라이언트-서버 구조이므로 서버에 벡터만 전송)
    model = SentenceTransformer(MODEL_NAME, device=DEVICE)
    
    # Milvus Upsert를 위한 리스트 분리 (Column-based 전송)
    sources, vectors, metadatas = [], [], []
    for item in changed_items:
        # 삭제 처리 (Expr 구문 사용)
        if item.get('is_deleted', False):
            collection.delete(f'source in ["{item["source"]}"]')
            print(f"🗑️ 삭제 처리됨 (Milvus 제거 완료): {item['source']}")
            continue
        
        if not item.get('text'): continue
        
        # 텍스트 벡터화
        vector = model.encode(item['text'], normalize_embeddings=True).tolist()
        sources.append(item['source'])
        vectors.append(vector)
        metadatas.append(item) # JSON 필드에 통째로 삽입

    if sources:
        print(f"🚀 [Milvus Upsert] {len(sources)}건의 데이터를 Milvus 서버에 전송합니다...")
        # Schema에 정의된 필드 순서대로 리스트의 리스트를 전달
        collection.upsert([sources, vectors, metadatas])
        # 즉시 검색 가능하도록 데이터 플러시(디스크/메모리 동기화)
        collection.flush()

    # 단건 동기화가 아닐 때만 타임스탬프 갱신
    if not target_source_id:
        STATE_FILE.write_text(json.dumps({"last_run": current_time}))

    print(f"[bold green]✅ [{datetime.now().strftime('%H:%M:%S')}] L5_Milvus 동기화 완료! (적용/삭제 처리: {len(changed_items)}건)[/bold green]")

if __name__ == "__main__":
    run_sync()