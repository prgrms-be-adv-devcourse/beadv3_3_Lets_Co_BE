"""
[L1_Flat] 데이터베이스 동기화 및 임베딩 스크립트
MariaDB의 데이터를 조회하여 텍스트를 추출하고, 타겟(나이/성별) 정보를 분석한 뒤
FAISS Flat 인덱스와 메타데이터 파일(.jsonl), 벡터 캐시(.npy)를 갱신합니다.
"""

import os
import json
import yaml
import pymysql
import faiss
import torch
import shutil
import base64
import re
import numpy as np
from pathlib import Path
from datetime import datetime
from sentence_transformers import SentenceTransformer
from Crypto.Cipher import AES
from rich import print

# ==========================================
# ⚙️ [1] 환경 설정 및 매핑 데이터 로드
# ==========================================
CONFIG_PATH = Path("../rag_config.yml")
TABLE_MAP_PATH = Path("../data/table_map.json")

# 필수 설정 파일 존재 여부 검사
if not CONFIG_PATH.exists():
    raise FileNotFoundError("rag_config.yml 파일을 찾을 수 없습니다.")
if not TABLE_MAP_PATH.exists():
    raise FileNotFoundError("data/table_map.json 파일을 찾을 수 없습니다.")

with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

with open(TABLE_MAP_PATH, "r", encoding="utf-8") as f:
    TABLE_MAP = json.load(f)

# 파일 저장 경로 및 AI 모델 설정 로드
INDEX_PATH = Path(cfg['paths']['index_root']) / "index.faiss"
META_PATH = Path(cfg['paths']['meta_file'])
CACHE_PATH = Path(cfg['paths']['index_root']) / "embeddings.npy"
MODEL_NAME = cfg['model']['embedding']
DEVICE = cfg['model']['device']

# ==========================================
# 🛠️ [2] 유틸리티 함수 (복호화 및 데이터 가공)
# ==========================================
def decrypt_aes_gcm(encrypted_base64, secret_key_str):
    """
    [보안] Java Spring 서버(AESUtil.java)에서 AES/GCM/NoPadding 방식으로 암호화된 데이터를 복호화합니다.
    """
    if not encrypted_base64:
        return None
    try:
        # Java 코드와 동일하게 32바이트 길이로 시크릿 키를 맞춤 처리
        raw_key = secret_key_str.encode('utf-8')
        key_bytes = bytearray(32)
        length = min(len(raw_key), 32)
        key_bytes[:length] = raw_key[:length]
        key = bytes(key_bytes)

        # Base64 디코딩 후 IV(12byte), 암호문 본문, Tag(16byte) 분리
        combined = base64.b64decode(encrypted_base64)
        iv = combined[:12]               
        ciphertext = combined[12:-16]    
        tag = combined[-16:]             

        # 복호화 수행
        cipher = AES.new(key, AES.MODE_GCM, nonce=iv)
        plaintext = cipher.decrypt_and_verify(ciphertext, tag)
        return plaintext.decode('utf-8')
    except Exception as e:
        print(f"[bold red]⚠️ [복호화 오류] 데이터 복호화 실패: {e}[/bold red]")
        return None

def get_age_group(birth_str):
    """
    생년월일 문자열(예: 1995-05-15)에서 현재 연도를 기준으로 연령대(예: 20대)를 계산합니다.
    """
    if not birth_str:
        return "연령 미상"
    try:
        # 숫자만 추출
        num_str = re.sub(r'[^0-9]', '', str(birth_str))
        if len(num_str) < 4:
            return "연령 미상"
        
        birth_year = int(num_str[:4])
        current_year = datetime.now().year
        age = current_year - birth_year
        # 10 단위로 내림하여 연령대 계산 (예: 28 -> 20대)
        return f"{(age // 10) * 10}대"
    except Exception:
        return "연령 미상"

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
# 📡 [3] DB 데이터 추출 및 전처리 로직
# ==========================================
def fetch_all_data(target_source_id=None):
    """
    MariaDB에서 모든 대상 데이터를 수집하고, 타겟(연령/성별) 정보를 계산하여 
    RAG 검색에 쓰일 메타데이터 페이로드(Payload) 형태로 구조화합니다.
    """
    conn = get_db_conn()
    changes = []
    
    # application.yml(Spring)과 맞춰진 시크릿 키 로드
    aes_secret_key = cfg.get('security', {}).get('aes256_key', 'YOUR_SECRET_KEY_HERE')

    print(f"[cyan]데이터베이스(MariaDB) 조회 시작... (단건 동기화 ID: {target_source_id or '없음 - 전체 조회'})[/cyan]")

    try:
        with conn.cursor() as cursor:
            # ----------------------------------------------------
            # [A] 상품별 타겟 분석 (실제 주문 내역 및 유저 정보 기반)
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
            # [B] 부가 데이터 사전 로딩 (옵션 및 QnA)
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
            # [C] 테이블 매핑 순회 및 최종 데이터 병합
            # ----------------------------------------------------
            for table, info in TABLE_MAP.items():
                if table == "Customer_Service_Detail": continue
                
                base_sql = info.get("query")
                params = []
                
                # 특정 데이터만 동기화(단건 동기화 API) 호출 시 쿼리 조건 추가
                if target_source_id:
                    t_name, t_idx = target_source_id.split(":")
                    if t_name != table: continue
                    connector = " AND " if "WHERE" in base_sql.upper() else " WHERE "
                    base_sql += f"{connector}{info['id']} = %s"
                    params.append(t_idx)
                
                cursor.execute(base_sql, params)
                rows = cursor.fetchall()
                
                for r in rows:
                    # RAG 검색에 쓰일 핵심 텍스트를 하나의 문자열로 결합
                    combined_text = " ".join([str(r[col.split('.')[-1]]) for col in info['text_cols'] if r.get(col.split('.')[-1])])
                    source_id = f"{table}:{r[info['id'].split('.')[-1]]}"
                    
                    # ⭐ [방어 로직] 오직 'Products' 데이터만 나이/성별 타겟팅을 가짐 (공지/QnA는 무조건 무관)
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

                    payload = {
                        "source": source_id,
                        "text": combined_text.strip(),
                        "is_deleted": (r.get('Del', 0) != 0),
                        "target_gender": target_gender,
                        "target_age": target_age
                    }

                    # 테이블 유형에 따라 필요한 부가 정보를 Payload에 담음
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
        
    print(f"[cyan]데이터 추출 완료! (추출된 데이터 수: {len(changes)}건)[/cyan]")
    return changes

# ==========================================
# 🚀 [4] FAISS 인덱스 업데이트 (증분 동기화)
# ==========================================
def run_sync(target_source_id=None):
    """
    기존에 임베딩된 캐시(.npy)를 재활용하여 변경된 데이터만 AI 임베딩을 수행하고,
    FAISS Flat 인덱스를 원자적(Atomic)으로 재작성하여 무중단 업데이트를 보장합니다.
    """
    print(f"[bold magenta][{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] L1_Flat 동기화 프로세스 가동 (증분 캐시 모드)[/bold magenta]")
    
    # 1. DB에서 최신 데이터 패치
    changed_items = fetch_all_data(target_source_id=target_source_id)
    if not changed_items and not target_source_id:
        print("[yellow]DB 변경 사항이 없습니다. 동기화를 종료합니다.[/yellow]")
        return

    # 2. 기존 메타데이터 및 임베딩 캐시 로드
    old_meta = []
    if META_PATH.exists():
        with open(META_PATH, "r", encoding="utf-8") as f:
            old_meta = [json.loads(line) for line in f]

    old_embeddings = None
    if CACHE_PATH.exists() and len(old_meta) > 0:
        try:
            old_embeddings = np.load(str(CACHE_PATH))
            if old_embeddings.shape[0] != len(old_meta):
                old_embeddings = None
        except Exception:
            old_embeddings = None

    # 빠른 조회를 위해 딕셔너리로 변환
    old_meta_dict = {}
    if old_embeddings is not None:
        for i, m in enumerate(old_meta):
            old_meta_dict[m['source']] = {'idx': i, 'text': m['text']}

    new_meta = []
    new_embeddings_list = []
    texts_to_encode = []
    meta_to_encode = []
    processed_sources = set()

    if target_source_id:
        processed_sources.add(target_source_id)

    # 3. 변경/추가/삭제 판단 로직
    for item in changed_items:
        src = item['source']
        processed_sources.add(src)

        # 삭제 마킹되었거나 텍스트가 없는 경우 인덱스에서 배제
        if item['is_deleted'] or not item['text']:
            continue

        # 텍스트가 이전과 동일하면 값비싼 GPU 임베딩 생략 (캐시 재사용)
        if old_embeddings is not None and src in old_meta_dict and old_meta_dict[src]['text'] == item['text']:
            idx = old_meta_dict[src]['idx']
            new_meta.append(item)
            new_embeddings_list.append(old_embeddings[idx:idx+1]) 
        else:
            # 텍스트가 새로 추가되거나 변경된 경우 임베딩 대기열에 추가
            texts_to_encode.append(item['text'])
            meta_to_encode.append(item)

    # 타겟 단건 동기화 시, 호출되지 않은 나머지 기존 데이터는 그대로 보존
    if target_source_id and old_embeddings is not None:
        for i, m in enumerate(old_meta):
            if m['source'] not in processed_sources:
                new_meta.append(m)
                new_embeddings_list.append(old_embeddings[i:i+1])

    # 4. 신규/변경 텍스트 AI 임베딩 생성 (GPU 병렬 처리)
    if texts_to_encode:
        print(f"🚀 [AI 임베딩 진행] 새롭게 변동된 {len(texts_to_encode)}건의 데이터를 벡터화합니다...")
        model = SentenceTransformer(MODEL_NAME, device=DEVICE)
        new_encoded = model.encode(texts_to_encode, normalize_embeddings=True).astype("float32")
        new_embeddings_list.append(new_encoded)
        new_meta.extend(meta_to_encode)

    # 5. 최종 FAISS 인덱스 조립
    if new_embeddings_list:
        final_embeddings = np.vstack(new_embeddings_list)
        dim = final_embeddings.shape[1]
    else:
        dim = 1024
        final_embeddings = np.empty((0, dim), dtype="float32")

    print(f"💾 [인덱스 생성] L1 Flat (내적-IP) 인덱스 생성 중... (최종 데이터: {final_embeddings.shape[0]}건)")
    index = faiss.IndexFlatIP(dim)
    if final_embeddings.shape[0] > 0:
        index.add(final_embeddings)

    # 6. 임시 파일(.tmp)로 저장 후 원자적(Atomic) 덮어쓰기 (읽기 락 방지)
    INDEX_PATH.parent.mkdir(parents=True, exist_ok=True)
    faiss.write_index(index, str(INDEX_PATH) + ".tmp")
    
    with open(str(META_PATH) + ".tmp", "w", encoding="utf-8") as f:
        for m in new_meta:
            f.write(json.dumps(m, ensure_ascii=False) + "\n")
            
    with open(str(CACHE_PATH) + ".tmp", "wb") as f:
        np.save(f, final_embeddings)

    shutil.move(str(INDEX_PATH) + ".tmp", str(INDEX_PATH))
    shutil.move(str(META_PATH) + ".tmp", str(META_PATH))
    shutil.move(str(CACHE_PATH) + ".tmp", str(CACHE_PATH))
    
    print(f"[bold green]✅ [{datetime.now().strftime('%H:%M:%S')}] L1_Flat 동기화 완료! (적용된 총 데이터: {len(new_meta)}건)[/bold green]")

if __name__ == "__main__":
    run_sync()