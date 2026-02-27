import pymysql
import random
import uuid
import base64
import bcrypt
from datetime import datetime, timedelta
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
from Crypto.Random import get_random_bytes
from faker import Faker

# ==========================================
# 📊 [1] 데이터 생성 수량 설정
# ==========================================
NUM_ADMINS = 10               # 관리자 수
NUM_SELLERS = 500             # 판매자 수
NUM_REGULAR_USERS = 4000      # 일반 유저 수
NUM_PRODUCTS = 10000          # 생성할 상품 수
NUM_ORDERS = 60000            # 생성할 주문 건수 (1건당 1~10개 품목 포함됨)
NUM_REVIEWS = 40000           # 작성될 리뷰 수
# (챗봇 및 CS 관련 수량 설정 제거됨)

# ==========================================
# 🎮 [2] 계층형 IP 트리 구조 (다양성 100% 보존)
# ==========================================
IP_TREE = {
    "영화코믹스": ["디즈니", "마블", "DC코믹스", "스타워즈", "해리포터", "반지의제왕", "픽사", "아바타", "주라기공원", "트랜스포머", "매트릭스", "분노의질주", "스파이더맨", "어벤져스", "다크나이트", "조커", "겨울왕국", "토이스토리", "인셉션", "인터스텔라"],
    "애니메이션캐릭터": ["카카오프렌즈", "라인프렌즈", "산리오", "포켓몬스터", "디지몬", "원피스", "나루토", "드래곤볼", "귀멸의칼날", "주술회전", "진격의거인", "스파이패밀리", "슬램덩크", "명탐정코난", "짱구는못말려", "도라에몽", "세일러문", "에반게리온", "이웃집토토로", "센과치히로", "하울의움직이는성", "너의이름은", "스즈메의문단속", "심슨가족", "스폰지밥", "미니언즈", "너구리라스칼", "먼작귀", "몰랑이", "뽀로로"],
    "게임": ["리그오브레전드", "발로란트", "오버워치", "스타크래프트", "디아블로", "워크래프트", "엘든링", "다크소울", "몬스터헌터", "바이오하자드", "파이널판타지", "드래곤퀘스트", "슈퍼마리오", "젤다의전설", "커비", "동물의숲", "소닉", "메이플스토리", "던전앤파이터", "로스트아크", "블루아카이브", "원신", "붕괴스타레일", "페이트", "철권", "스트리트파이터", "마인크래프트", "로블록스", "배틀그라운드", "사이버펑크2077"],
    "글로벌브랜드": ["레고", "바비", "핫휠", "다마고치", "헬로키티", "쿠로미", "시나모롤", "브라운", "라이언", "어피치", "나이키", "아디다스", "슈프림", "애플", "삼성", "소니", "닌텐도", "나사", "내셔널지오그래픽", "파타고니아"]
}

ADJECTIVES = ["프리미엄", "시그니처", "베이직", "클래식", "리미티드 에디션", "스페셜", "컴팩트", "에센셜", "데일리", "오리지널"]

# ==========================================
# 🏷️ [3] 카테고리별 맞춤형 옵션 풀
# ==========================================
OPTION_POOLS = {
    "패션의류": [["S", "M", "L", "XL", "XXL"], ["블랙", "화이트", "그레이", "네이비", "베이지"]],
    "가전디지털": [["스페이스 그레이", "실버", "매트 블랙", "스타라이트", "알파인 그린"], ["기본형", "고급형", "풀패키지", "보호필름 포함", "충전기 포함"]],
    "생활인테리어": [["소형", "중형", "대형", "특대형", "맞춤형"], ["퓨어 화이트", "내추럴 우드", "다크 월넛", "라이트 오크", "모던 차콜"]],
    "식품": [["1개입", "3개입", "5개입", "10개입", "1박스"], ["오리지널", "매운맛", "순한맛", "치즈맛", "바베큐맛"]]
}

# ==========================================
# 💰 [4] 세부 카테고리별 정밀 가격 맵 (현실성 보존)
# ==========================================
SUB_CATEGORY_PRICE_MAP = {
    "티셔츠": (15000, 45000, 1000), "셔츠": (25000, 75000, 1000), "후드티": (35000, 95000, 1000),
    "스마트폰": (800000, 2100000, 10000), "노트북": (900000, 3800000, 50000),
    "냉장고": (1200000, 5500000, 100000), "라면": (3500, 15000, 500), "한우": (50000, 250000, 5000)
}
LARGE_CATEGORY_FALLBACK = {
    "패션의류": (30000, 100000, 1000), "가전디지털": (100000, 1000000, 5000),
    "생활인테리어": (50000, 500000, 1000), "식품": (5000, 50000, 500)
}

# ==========================================
# 🖼️ [5] S3 이미지 샘플 매핑 데이터
# ==========================================
S3_SAMPLE_IMAGES = [
    {"file_name": "f47ac10b-58cc-4372-a567-0e02b2c3d479.jpeg", "mime": "image/jpeg"},
    {"file_name": "c2049975-fcce-4eb6-9883-93e11041c2c3.jpeg", "mime": "image/jpeg"},
    {"file_name": "81f190e2-63b1-4f10-ad27-92cf9d7e35b1.jpeg", "mime": "image/jpeg"},
    {"file_name": "3d9e8400-e29b-41d4-a716-446655440000.jpeg", "mime": "image/jpeg"},
    {"file_name": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d.jpeg", "mime": "image/jpeg"},
    {"file_name": "e5c146fb-01cb-4f39-93f4-0b7db91384f5.jpeg", "mime": "image/jpeg"},
    {"file_name": "7a353d2d-0b1a-4ab0-b855-38b43825709a.jpeg", "mime": "image/jpeg"},
    {"file_name": "b59a60e0-3221-4d37-8898-0c6ef2cf8562.jpeg", "mime": "image/jpeg"},
    {"file_name": "d2a93864-44b2-4cd8-8422-777995eb8886.jpeg", "mime": "image/jpeg"},
    {"file_name": "123e4567-e89b-12d3-a456-426614174000.jpeg", "mime": "image/jpeg"}
]

# ==========================================
# ⚙️ [6] DB 접속 및 보안 설정
# ==========================================
DB_CONFIG = {
    'host': '',
    'port': ,
    'user': '',
    'password': '',
    'db': '',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

AES_KEY_STR = ""
AES_KEY = AES_KEY_STR.encode('utf-8')[:32].ljust(32, b'\0')
FIXED_IV_CBC = b'\0' * 16 

fake = Faker('ko_KR')

def encrypt_gcm(text):
    if not text: return None
    iv = get_random_bytes(12)
    cipher = AES.new(AES_KEY, AES.MODE_GCM, nonce=iv)
    ciphertext, tag = cipher.encrypt_and_digest(text.encode('utf-8'))
    return base64.b64encode(iv + ciphertext + tag).decode('utf-8')

def encrypt_cbc(text):
    if not text: return None
    cipher = AES.new(AES_KEY, AES.MODE_CBC, iv=FIXED_IV_CBC)
    return base64.b64encode(cipher.encrypt(pad(text.encode('utf-8'), 16))).decode('utf-8')

def hash_pw(pw):
    return bcrypt.hashpw(base64.b64encode(pw.encode('utf-8')), bcrypt.gensalt(4)).decode('utf-8')

def get_rand_date(days_back=365):
    return datetime.now() - timedelta(days=random.randint(0, days_back), hours=random.randint(0, 23))

# ==========================================
# 🚀 [7] 메인 데이터 생성 함수
# ==========================================
def main():
    conn = pymysql.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    try:
        cur.execute("SET FOREIGN_KEY_CHECKS = 0;")
        
        # 1. 테이블 청소 (CS, 챗봇 테이블도 깨끗하게 비웁니다)
        print("\n🧹 1단계: 기존 데이터 초기화 중...")
        tables = ['Assistant_Chat', 'Assistant', 'File', 'Customer_Service_Detail', 'Customer_Service', 'Review', 'Settlement_History', 'Payment', 'Orders_Item', 'Orders', 'Product_Option', 'Products', 'Seller', 'Users_Verifications', 'Users_Card', 'Users_Address', 'Users_Information', 'Users', 'Products_Category']
        for t in tables: cur.execute(f"DELETE FROM `{t}`")
        print("   ✅ 테이블 초기화 완료")

        # 2. 카테고리 트리
        print("\n📁 2단계: 카테고리 트리 생성 (0/ 루트 경로 적용)...")
        category_tree = {
            "패션의류": {"상의": ["티셔츠", "셔츠", "스웨터", "후드티", "맨투맨"], "하의": ["청바지", "슬랙스", "면바지", "트레이닝팬츠"], "아우터": ["코트", "패딩", "자켓", "가디건"]},
            "가전디지털": {"휴대폰": ["스마트폰", "스마트폰케이스", "고속충전기", "보조배터리"], "컴퓨터": ["노트북", "데스크탑", "모니터", "무선마우스", "기계식키보드"], "주방가전": ["냉장고", "전자레인지", "밥솥", "커피머신"]},
            "생활인테리어": {"가구": ["침대", "소파", "책상", "의자"], "침구": ["이불", "베개", "매트리스패드"], "조명": ["천장등", "스탠드", "무드등"]},
            "식품": {"가공식품": ["라면", "통조림", "즉석밥", "단백질바"], "신선식품": ["유기농사과", "샐러드키트", "한우", "연어"]}
        }
        small_category_cache = []
        for large, mids in category_tree.items():
            cur.execute("INSERT INTO Products_Category (Type, Path, Category_Code, Category_Name) VALUES ('CATEGORY', '', %s, %s)", (f"CAT_{uuid.uuid4().hex[:6].upper()}", large))
            l_idx = cur.lastrowid
            l_path = f"0/{l_idx}"
            cur.execute("UPDATE Products_Category SET Path = %s WHERE Category_IDX = %s", (l_path, l_idx))
            for mid, smalls in mids.items():
                cur.execute("INSERT INTO Products_Category (Type, Path, Category_Code, Category_Name, Parent_IDX) VALUES ('CATEGORY', '', %s, %s, %s)", (f"CAT_{uuid.uuid4().hex[:6].upper()}", mid, l_idx))
                m_idx = cur.lastrowid
                m_path = f"{l_path}/{m_idx}"
                cur.execute("UPDATE Products_Category SET Path = %s WHERE Category_IDX = %s", (m_path, m_idx))
                for small in smalls:
                    cur.execute("INSERT INTO Products_Category (Type, Path, Category_Code, Category_Name, Parent_IDX) VALUES ('CATEGORY', '', %s, %s, %s)", (f"CAT_{uuid.uuid4().hex[:6].upper()}", small, m_idx))
                    s_idx = cur.lastrowid
                    s_path = f"{m_path}/{s_idx}"
                    cur.execute("UPDATE Products_Category SET Path = %s WHERE Category_IDX = %s", (s_path, s_idx))
                    small_category_cache.append({'idx': s_idx, 'name': small, 'large_name': large})
        print("   ✅ 카테고리 트리 완성")

        # 3. IP 트리
        print("\n🏷️ 3단계: 브랜드 IP 계층 트리 생성 중...")
        ip_cache = []
        for p_ip, c_ips in IP_TREE.items():
            cur.execute("INSERT INTO Products_Category (Type, Path, Category_Code, Category_Name) VALUES ('IP', '', %s, %s)", (f"IP_{uuid.uuid4().hex[:6].upper()}", p_ip))
            p_idx = cur.lastrowid
            p_path = f"0/{p_idx}"
            cur.execute("UPDATE Products_Category SET Path = %s WHERE Category_IDX = %s", (p_path, p_idx))
            for c_ip in c_ips:
                cur.execute("INSERT INTO Products_Category (Type, Path, Category_Code, Category_Name, Parent_IDX) VALUES ('IP', '', %s, %s, %s)", (f"IP_{uuid.uuid4().hex[:6].upper()}", c_ip, p_idx))
                c_idx = cur.lastrowid
                cur.execute("UPDATE Products_Category SET Path = %s WHERE Category_IDX = %s", (f"{p_path}/{c_idx}", c_idx))
                ip_cache.append({'idx': c_idx, 'name': c_ip})
        print(f"   ✅ IP 데이터 생성 완료 ({len(ip_cache)}개 브랜드)")

        # 4. 유저 및 배송지
        total_users = NUM_ADMINS + NUM_SELLERS + NUM_REGULAR_USERS
        print(f"\n👤 4단계: 유저 및 배송지 생성 중 (총 {total_users}명)...")
        user_pool = []
        user_address_map = {}
        roles = (['ADMIN']*NUM_ADMINS) + (['SELLER']*NUM_SELLERS) + (['USERS']*NUM_REGULAR_USERS)
        
        for i, r in enumerate(roles):
            u_created = datetime.now() - timedelta(days=random.randint(500, 600))
            cur.execute("INSERT INTO Users (ID, PW, Role, Created_at) VALUES (%s, %s, %s, %s)", (encrypt_cbc(f"user_{i}_{uuid.uuid4().hex[:4]}"), hash_pw("pass123!"), r, u_created))
            u_idx = cur.lastrowid
            user_pool.append(u_idx)
            
            # 💡 생일 17세 이상, YYYY-MM-DD 포맷, 성별 6:4 비율 완벽 적용
            birth_date_str = fake.date_of_birth(minimum_age=17, maximum_age=80).strftime('%Y-%m-%d')
            gender_val = 'MALE' if random.random() < 0.6 else 'FEMALE'

            cur.execute("INSERT INTO Users_Information (Users_IDX, Mail, Name, Phone_Number, Birth, Gender, Balance) VALUES (%s,%s,%s,%s,%s,%s,%s)", 
                        (u_idx, encrypt_gcm(fake.email()), encrypt_gcm(fake.name()), encrypt_gcm(fake.phone_number()), encrypt_gcm(birth_date_str), gender_val, random.randint(10000, 1000000)))
            
            user_address_map[u_idx] = []
            for _ in range(random.randint(1, 3)):
                addr_info = (u_idx, uuid.uuid4().hex, encrypt_gcm(fake.name()), encrypt_gcm(fake.address()), encrypt_gcm(fake.phone_number()))
                cur.execute("INSERT INTO Users_Address (Users_IDX, Address_Code, Recipient, Address, Phone_Number) VALUES (%s,%s,%s,%s,%s)", addr_info)
                user_address_map[u_idx].append({'recipient': addr_info[2], 'address': addr_info[3], 'phone': addr_info[4]})
            
            if (i+1) % 1000 == 0: 
                print(f"   -> [진행중] 유저 {i+1}명 완료..."); conn.commit()

        # 5. 판매자 정보
        print(f"\n🏢 5단계: 판매자 및 사업자 정보 생성 중...")
        seller_pool = []
        for idx, u_idx in enumerate(user_pool[NUM_ADMINS : NUM_ADMINS+NUM_SELLERS]):
            raw_name = fake.company()
            biz_license = f"{idx:03d}-88-{random.randint(10000, 99999)}"
            cur.execute("INSERT INTO Seller (Users_IDX, Seller_Name, Business_License, Bank_Brand, Bank_Name, Bank_Token) VALUES (%s,%s,%s,%s,%s,%s)",
                        (u_idx, encrypt_gcm(raw_name), encrypt_gcm(biz_license), encrypt_gcm("국민은행"), encrypt_gcm(fake.name()), encrypt_gcm(uuid.uuid4().hex)))
            seller_pool.append(cur.lastrowid)
        conn.commit(); print("   ✅ 판매자 500명 생성 완료")

        # 6. 상품 및 옵션
        print(f"\n📦 6단계: 세부 가격 기반 상품 및 옵션 생성 중 (총 {NUM_PRODUCTS}개)...")
        product_cache = []
        for i in range(NUM_PRODUCTS):
            ip_obj, cat_obj = random.choice(ip_cache), random.choice(small_category_cache)
            p_min, p_max, p_step = SUB_CATEGORY_PRICE_MAP.get(cat_obj['name'], LARGE_CATEGORY_FALLBACK.get(cat_obj['large_name'], (10000, 100000, 1000)))
            base_price = random.randrange(p_min, p_max + 1, p_step)
            sale_price = base_price * 0.9 if random.random() < 0.2 else 0
            
            p_name = f"[{ip_obj['name']}] {random.choice(ADJECTIVES)} {cat_obj['name']}"
            cur.execute("INSERT INTO Products (Seller_IDX, Products_Code, Products_Category, Products_IP, Products_Name, Price, Sale_Price, Stock, Status) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,'ON_SALE')",
                        (random.choice(seller_pool), uuid.uuid4().hex, cat_obj['idx'], ip_obj['idx'], p_name, base_price, sale_price, random.randint(100, 500)))
            p_idx = cur.lastrowid
            
            img = random.choice(S3_SAMPLE_IMAGES)
            cur.execute("INSERT INTO `File` (`File_Origin`, `File_Name`, `File_Type`, `File_Path`, `Ref_Table`, `Ref_Index`) VALUES (%s,%s,%s,'Media/productImage/', 'Products', %s)", (p_name, img['file_name'], img['mime'], p_idx))

            created_opts = []
            opt_pool = random.choice(OPTION_POOLS.get(cat_obj['large_name'], [["기본 옵션"]]))
            for o_n in ["기본"] + random.sample(opt_pool, min(2, len(opt_pool))):
                o_price = base_price if o_n == "기본" else base_price + int(base_price * random.uniform(0.05, 0.15))
                o_code = f"OPT_{uuid.uuid4().hex[:10].upper()}"
                cur.execute("INSERT INTO Product_Option (Products_IDX, Option_Code, Option_Name, Option_Price, Stock, Status) VALUES (%s,%s,%s,%s,999,'ON_SALE')", (p_idx, o_code, o_n, o_price))
                created_opts.append({'code': o_code, 'name': o_n, 'price': o_price})
            
            product_cache.append({'idx': p_idx, 'code': uuid.uuid4().hex, 'name': p_name, 'options': created_opts, 'seller_idx': random.choice(seller_pool)})
            if (i+1) % 2000 == 0: 
                print(f"   -> [진행중] 상품 {i+1}개 완료..."); conn.commit()

        # 7. 주문 시뮬레이션
        print(f"\n🛒 7단계: 주문, 배송지 맵핑, 결제, 정산 처리 중 (총 {NUM_ORDERS}건)...")
        purchased_items = []
        for i in range(NUM_ORDERS):
            u_idx = random.choice(user_pool[NUM_ADMINS+NUM_SELLERS:])
            ord_date = datetime.now() - timedelta(days=random.randint(1, 365))
            u_addr = random.choice(user_address_map[u_idx]) # 배송지 정보 동기화 완료
            
            num_cart = random.randint(1, 7) # 장바구니 1~10개 품목
            cart_items = random.sample(product_cache, num_cart)
            total_amt = 0; seller_sums = {}; temp_items = []
            
            for itm in cart_items:
                opt = random.choice(itm['options']); qty = random.randint(1, 2); item_price = opt['price'] * qty
                total_amt += item_price; seller_sums[itm['seller_idx']] = seller_sums.get(itm['seller_idx'], 0) + item_price
                temp_items.append((itm, opt, qty))

            cur.execute("INSERT INTO Orders (Orders_Code, Users_IDX, Recipient, Address, Phone_Number, Status, Items_Amount, Total_Amount, Created_at) VALUES (%s,%s,%s,%s,%s,'PAID',%s,%s,%s)", 
                        (uuid.uuid4().hex, u_idx, u_addr['recipient'], u_addr['address'], u_addr['phone'], total_amt, total_amt, ord_date))
            o_idx = cur.lastrowid
            
            for itm, opt, qty in temp_items:
                cur.execute("INSERT INTO Orders_Item (Orders_IDX, Products_Code, Products_Name, Option_Code, Option_Name, Price, Quantity, Created_at) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)", (o_idx, itm['code'], itm['name'], opt['code'], opt['name'], opt['price'], qty, ord_date))
                purchased_items.append({'p': itm['idx'], 'u': u_idx, 'oi': cur.lastrowid, 'date': ord_date})
            
            cur.execute("INSERT INTO Payment (Users_IDX, Status, Type, Amount, Orders_IDX, Created_at) VALUES (%s,'PAYMENT','CARD',%s,%s,%s)", (u_idx, total_amt, o_idx, ord_date))
            p_idx = cur.lastrowid
            for sidx, samt in seller_sums.items():
                cur.execute("INSERT INTO Settlement_History (Seller_IDX, Type, Payment_IDX, Amount, Created_at) VALUES (%s,'ORDERS_CONFIRMED',%s,%s,%s)", (sidx, p_idx, samt, ord_date))
            
            if (i+1) % 10000 == 0: 
                print(f"   -> [진행중] 주문 트랜잭션 {i+1}건 완료..."); conn.commit()

        # 8. 리뷰 생성 (CS 관련 삽입 제거)
        print(f"\n⭐ 8단계: 구매 이력 기반 리뷰 데이터 생성 중 (CS 데이터는 생성하지 않음)...")
        for r in random.sample(purchased_items, min(NUM_REVIEWS, len(purchased_items))):
            r_dt = min(r['date'] + timedelta(days=random.randint(1, 10)), datetime.now())
            cur.execute("INSERT INTO Review (Products_IDX, Users_IDX, Orders_Item_IDX, Evaluation, Content, Created_at) VALUES (%s,%s,%s,%s,%s,%s)", (r['p'], r['u'], r['oi'], random.randint(4, 5), fake.sentence(), r_dt))
        print("   ✅ 리뷰 데이터 생성 완료")

        # 9. 최종 완료 (챗봇 시뮬레이션 제거됨)
        print("\n💾 9단계: 모든 트랜잭션 최종 커밋 및 제약조건 복구 중...")
        conn.commit()
        cur.execute("SET FOREIGN_KEY_CHECKS = 1;")
        print(f"\n✨🎉 [최종 성공] 챗봇 및 CS를 제외한 약 50만 건 이상의 리얼 데이터가 완벽하게 구축되었습니다!")

    except Exception as e:
        conn.rollback(); print(f"\n❌ 작업 중 치명적 오류 발생: {e}")
    finally:
        conn.close()

if __name__ == "__main__": main()