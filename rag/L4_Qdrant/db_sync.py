"""
[L4_Qdrant] 데이터베이스 동기화 및 벡터 DB 업서트(Upsert) 스크립트
MariaDB의 데이터를 추출하고 타겟(나이/성별) 정보를 분석한 뒤,
Qdrant 벡터 데이터베이스에 Point(벡터+메타데이터 페이로드) 형태로 저장합니다.
타임스탬프(Updated_at) 기반의 실시간 증분 동기화를 완벽하게 지원합니다.
"""

import json
import yaml
import pymysql
import uuid
import re
import base64
from pathlib import Path
from datetime import datetime
from sentence_transformers import SentenceTransformer
from qdrant_client import QdrantClient
from qdrant_client.http import models as qmodels
from qdrant_client.models import Distance, VectorParams, PointStruct
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

# Qdrant 클라이언트 연결 설정 및 증분 동기화 상태 파일
client = QdrantClient(
    host=cfg['vector_db']['qdrant']['host'], 
    port=cfg['vector_db']['qdrant']['port']
)
COLLECTION_NAME = cfg['vector_db']['qdrant']['collection']
STATE_FILE = Path("./sync_state.json")

# ==========================================
# 🛠️ [2] 유틸리티 함수 (복호화 및 연령 계산)
# ==========================================
def decrypt_aes_gcm(encrypted_base64, secret_key_str):
    """
    [보안] Java Spring 서버와 동일한 규격(AES/GCM/NoPadding)으로 고객 민감 정보를 복호화합니다.
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
    """생년월일(예: 1990-01-01)에서 현재 연도 기준의 10단위 연령대(예: 30대)를 산출합니다."""
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

def get_db_conn():
    """MariaDB 데이터베이스 커넥션을 반환합니다."""
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
# 📡 [3] DB 데이터 추출 및 Qdrant Payload 전처리
# ==========================================
def fetch_changed_data(last_time=None, target_source_id=None):
    """
    타임스탬프(Updated_at)를 기반으로 변경된 데이터만 추출합니다.
    추출된 데이터에 타겟(연령/성별) 정보를 분석하여 Qdrant Payload 규격으로 반환합니다.
    """
    conn = get_db_conn()
    changes = []
    aes_secret_key = cfg.get('security', {}).get('aes256_key', 'YOUR_SECRET_KEY_HERE_EXACTLY_SAME')
    
    print(f"[cyan]데이터베이스(MariaDB) 증분 조회 시작... (L4_Qdrant 대상)[/cyan]")
    if last_time:
        print(f"기준 시간 (Updated_at > {last_time}) 이후 변경된 데이터만 가져옵니다.")

    try:
        with conn.cursor() as cursor:
            # ----------------------------------------------------
            # [A] 상품별 타겟 분석 (실제 주문 데이터 기준 다수결)
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
            # [B] 상품 옵션 사전 로드
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

            # ----------------------------------------------------
            # [C] QnA 데이터 사전 로드 (질문 및 답변)
            # ----------------------------------------------------
            cursor.execute("SELECT Customer_Service_IDX, Content FROM Customer_Service_Detail WHERE Parent_IDX IS NOT NULL AND Del = 0")
            ans_dict = {row['Customer_Service_IDX']: row['Content'] for row in cursor.fetchall()}
            cursor.execute("SELECT Customer_Service_IDX, Content FROM Customer_Service_Detail WHERE Parent_IDX IS NULL AND Del = 0")
            q_dict = {row['Customer_Service_IDX']: row['Content'] for row in cursor.fetchall()}

            # ----------------------------------------------------
            # [D] 테이블 매핑 순회 및 증분 데이터 병합
            # ----------------------------------------------------
            for table, info in TABLE_MAP.items():
                if table == "Customer_Service_Detail": continue
                
                base_sql = info.get("query")
                
                # 삭제(Del=1) 이력을 추적하여 Qdrant에서 지우기 위해 Del=0 하드 조건을 임시 제거
                base_sql = re.sub(r'(?i)\bAND\s+(cs\.)?Del\s*=\s*0\b', '', base_sql)
                
                params = []
                where_clauses = []
                
                # 1) 특정 ID 단건 동기화 조건
                if target_source_id:
                    t_name, t_idx = target_source_id.split(":")
                    if t_name != table: continue
                    where_clauses.append(f"{info['id']} = %s")
                    params.append(t_idx)
                # 2) 타임스탬프 증분 동기화 조건
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
                    
                    # ⭐ [버그 패치 완벽 반영] 오직 'Products'일 때만 나이/성별을 부여하여 오버 필터링 방지
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

                    # Qdrant Payload로 들어갈 메타데이터 구조
                    payload = {
                        "source": source_id,
                        "text": combined_text.strip(),
                        "is_deleted": (r.get('Del', 0) != 0),
                        "target_gender": target_gender,
                        "target_age": target_age
                    }

                    if table == "Products":
                        payload.update({
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
                        payload.update({
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
                    changes.append(payload)
    finally:
        conn.close()
        
    print(f"[cyan]데이터 추출 완료! (대상 데이터 수: {len(changes)}건)[/cyan]")
    return changes

# ==========================================
# 🚀 [4] Qdrant 컬렉션 갱신 및 업서트(Upsert)
# ==========================================
def run_sync(target_source_id=None):
    """
    MariaDB 변경 사항을 확인하여 삭제된 데이터는 Qdrant에서 제거하고,
    추가/변경된 데이터는 벡터 임베딩 후 Qdrant에 Upsert 합니다.
    """
    # 1. 컬렉션 존재 여부 확인 및 생성 (1024차원, 코사인 유사도)
    if not client.collection_exists(COLLECTION_NAME):
        print(f"[yellow]⚠️ Qdrant 컬렉션({COLLECTION_NAME})이 존재하지 않아 새로 생성합니다.[/yellow]")
        client.create_collection(
            collection_name=COLLECTION_NAME,
            vectors_config=VectorParams(size=1024, distance=Distance.COSINE)
        )

    print(f"[bold magenta][{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] L4_Qdrant 동기화 가동 (타임스탬프 증분 모드)[/bold magenta]")
    
    # 2. 마지막 동기화 기준 시간 로드
    last_run = "1970-01-01 00:00:00"
    if STATE_FILE.exists():
        last_run = json.loads(STATE_FILE.read_text()).get("last_run", last_run)
    
    current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    # 3. 데이터 추출
    changed_items = fetch_changed_data(last_run if not target_source_id else None, target_source_id)

    if not changed_items and not target_source_id:
        print("[yellow]DB 변경 사항이 없습니다. 동기화를 종료합니다.[/yellow]")
        return
    elif not changed_items and target_source_id:
        # 단건 타겟인데 DB에 없으면 완전 삭제된 데이터로 간주
        changed_items = [{"source": target_source_id, "is_deleted": True}]

    # 4. 임베딩 모델 로드 (Qdrant는 클라이언트-서버 구조이므로 임베딩은 로컬에서 수행)
    model = SentenceTransformer(cfg['model']['embedding'], device=cfg['model']['device'])
    
    points = []
    for item in changed_items:
        # Qdrant는 UUID 형식의 ID를 요구하므로 source_id를 기반으로 고유 UUID5 생성
        point_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, item['source']))
        
        # 삭제 처리
        if item.get('is_deleted', False):
            client.delete(collection_name=COLLECTION_NAME, points_selector=[point_id])
            print(f"🗑️ 삭제 처리됨 (Qdrant 제거 완료): {item['source']}")
            continue
        
        if not item.get('text'): continue
        
        # 텍스트 벡터화 및 PointStruct 조립
        vector = model.encode(item['text'], normalize_embeddings=True).tolist()
        points.append(PointStruct(
            id=point_id,
            vector=vector,
            payload=item # 메타데이터 전체를 Payload로 주입
        ))

    # 5. Qdrant 서버로 대량 Upsert 전송
    if points:
        print(f"🚀 [Qdrant Upsert] {len(points)}건의 임베딩 데이터를 Qdrant 서버에 전송합니다...")
        client.upsert(collection_name=COLLECTION_NAME, points=points)

    # 6. 동기화 성공 시 타임스탬프 갱신 (단건 동기화가 아닐 때만)
    if not target_source_id:
        STATE_FILE.write_text(json.dumps({"last_run": current_time}))

    print(f"[bold green]✅ [{datetime.now().strftime('%H:%M:%S')}] L4_Qdrant 증분 동기화 완료! (적용/삭제 처리: {len(changed_items)}건)[/bold green]")

if __name__ == "__main__":
    run_sync()