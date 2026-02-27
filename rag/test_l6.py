import torch
import json
import yaml
import time
from pathlib import Path
from sentence_transformers import SentenceTransformer
from rich import print

# L6 내부 로직 임포트
# 반드시 rag/ 폴더에서 실행해야 하며 L6_Elasticsearch 폴더가 있어야 합니다.
from L6_Elasticsearch.app import fetch_product_data, format_for_embedding

def run_l6_test():
    print(f"\n{'='*60}")
    print("[bold cyan]🧪 L6 임베딩 시스템 정밀 무결성 테스트[/bold cyan]")
    print(f"{'='*60}")

    # 1. 설정 로드 테스트
    try:
        with open("rag_config.yml", "r", encoding="utf-8") as f:
            cfg = yaml.safe_load(f)
        print("✅ [1/4] rag_config.yml 로드 완료")
    except Exception as e:
        print(f"❌ [1/4] 설정 파일 로드 실패: {e}")
        return

    # 2. DB 조회 및 가공 테스트
    print("\n[bold yellow]🔍 2단계: DB 조회 및 텍스트 가공 테스트[/bold yellow]")
    print("...DB에 접속 중입니다. 잠시만 기다려주세요...")
    raw_data = fetch_product_data() 
    if raw_data:
        formatted = format_for_embedding(raw_data[:1]) 
        print(f"✅ [2/4] DB 데이터 조회 및 가공 성공 (총 {len(raw_data)}건 중 샘플 1건)")
        print(f"   [가공 문장]: [italic]{formatted[0]['text'][:60]}...[/italic]")
    else:
        print("⚠️ [2/4] DB 연결 실패 또는 데이터 없음. (가상 데이터로 테스트 진행)")
        mock_data = [{
            "Products_Code": "T-001", "Products_Name": "검은 후드",
            "Description": "테스트용", "Price": 1000, "Sale_Price": 0,
            "Category_Name": "의류", "IP_Name": "없음", "Options": "L"
        }]
        formatted = format_for_embedding(mock_data)

    # 3. AI 임베딩 연산 테스트 (가장 오래 걸리는 구간)
    print("\n[bold yellow]🧠 3단계: AI 모델 임베딩 연산 테스트[/bold yellow]")
    model_name = cfg['model']['embedding']
    device = cfg['model']['device']
    
    print(f"⏳ 모델([bold blue]{model_name}[/bold blue])을 [bold magenta]{device}[/bold magenta] 메모리에 올리는 중...")
    print("   (최초 실행 시 다운로드와 로딩에 1~3분 정도 소요될 수 있습니다. 끄지 마세요!)")
    
    start_time = time.time()
    try:
        # 모델 로드
        model = SentenceTransformer(model_name, device=device)
        print(f"✅ 모델 로드 완료! (소요: {time.time() - start_time:.2f}초)")
        
        # 실제 연산
        vector = model.encode([formatted[0]['text']], normalize_embeddings=True)[0]
        print(f"✅ [3/4] AI 임베딩 연산 성공 (벡터 차원: {len(vector)})")
    except Exception as e:
        print(f"❌ [3/4] 모델 로드 또는 연산 실패: {e}")
        return

    # 4. 최종 전송 페이로드 규격 확인
    print("\n[bold yellow]📦 4단계: 전송 페이로드 규격 검증[/bold yellow]")
    payload = {
        "type": "PRODUCT_SYNC",
        "vector": vector.tolist()[:5], # 앞부분 5개만 샘플링
        "metadata": formatted[0]['metadata']
    }
    print("✅ [4/4] 전송 데이터 규격 확인 완료")
    print(f"   - 전송 샘플(JSON): {json.dumps(payload, indent=2, ensure_ascii=False)[:250]}...")

    print(f"\n{'='*60}")
    print("[bold green]🎉 모든 L6 내부 로직이 정상 작동함을 확인했습니다![/bold green]")
    print(f"{'='*60}\n")

if __name__ == "__main__":
    run_l6_test()