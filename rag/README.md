# 🧠 GutJJeu RAG Service — WSL2 Ubuntu + venv (L1~L6 멀티 레벨 RAG)

Windows 11에서 **WSL2(Ubuntu) 가상화**를 사용하고, 그 안에서 **venv(파이썬 가상환경)** 로 패키지를 격리하여 실행하는 **RAG(Retrieval‑Augmented Generation) 검색 서비스**입니다.  
DB(MariaDB/MySQL)에서 데이터를 가져와 **임베딩 → 벡터 인덱싱 → 검색/동기화 API 제공**까지를 자동화합니다.

---

## 📚 목차

1. 보안/프라이버시(마스킹 규칙)
2. 시스템이 하는 일(개념/용어/흐름)
3. 권장 실행 환경(검증 환경 포함)
4. 최신 파일 구조 & “생성되는 파일”까지 설명
5. 처음 실행(WSL 설치부터) — 완전 상세
6. venv 사용 원칙(왜 필요한지/실수 방지)
7. 의존성 설치 — **A/B/C 옵션 + CPU/GPU 분기(복원/강화)**
8. 설치 검증 체크리스트(실행 전에 무조건 통과)
9. rag_config.yml — 섹션별 의미/튜닝 가이드(상세)
10. 실행 방법(권장/레벨 단독/장기 실행)
11. API 사용 예시(`/search`, `/sync-item`, `/embed-query`)
12. 운영 안전장치(필수: workers=1, VRAM/OOM, 큐잉)
13. Reset/복구(가벼운 리셋 → 완전 초기화)
14. Stress Test — 코드/데이터/재현/리포트 해석(상세)
15. License (상세)
16. World‑Class 문서 품질 기준(Release Checklist)

---

## 1) 🔒 보안/프라이버시(필수 규칙)

이 README는 외부 공유 시 운영 정보가 노출되지 않도록 아래 원칙을 **문서 레벨에서 강제**합니다.

### 1-1. 마스킹 규칙

- 외부 공개 주소/도메인/IP/포트/토큰/DB 계정은 문서에 **직접 적지 않습니다.**
- 외부 주소 예시는 다음처럼 **마스킹 표기**를 사용합니다.
  - `http://DDNS:포트번호/search`
  - `http://DDNS:포트번호/sync-item/{source_id}`
  - `http://DDNS:포트번호/embed-query`

### 1-2. 로컬 개발 주소

로컬 개발 예시(`localhost`)는 외부 공유 위험이 낮으므로 그대로 사용합니다.

- `http://localhost:8077` (RAG 엔진)
- `http://localhost:8078` (L6 서버)

### 1-3. 테스트 리포트 공유 주의

`rag_test_report_*.txt` 같은 리포트는 **에러 메시지/환경 정보**가 포함될 수 있습니다.

- 외부 공유 시 반드시 마스킹(주소/포트/키/계정/토큰)
- Git 커밋은 지양(권장: `.gitignore`)

---

## 2) 시스템이 하는 일(개념/용어/흐름)

### 2-1. RAG 핵심: “검색 + 생성”에서 ‘검색’ 품질을 끌어올리는 구조

이 서비스는 “사용자 질문에 맞는 문서/상품/CS 답변 후보를 잘 찾는 것”을 목표로 합니다.

### 2-2. 검색 파이프라인(정확히 어떤 흐름인가?)

**BGE‑M3(임베딩)** 로 1차 검색(FAISS/VectorDB) → **BGE‑Reranker‑v2‑m3(재정렬)** 로 2차 정렬을 수행합니다.

1. 사용자가 Query를 보냄
2. Query를 **BGE‑M3**로 임베딩(벡터화)
3. 벡터 인덱스에서 Top‑K 후보 검색(L1~L5: FAISS/Qdrant/Milvus)
4. 후보를 **BGE‑Reranker‑v2‑m3**로 재정렬(정확도 ↑)
5. 최종 Top‑K 결과 반환

### 2-3. 핵심 용어(리포트/운영에서 자주 등장)

- **SLA(3초)**: “3초 이내면 성공으로 간주”하는 서비스 수준 기준
- **TPS**: 초당 처리량(Throughput)
- **P99 / Max**: tail latency(끝단 지연). 운영 사고는 평균이 아니라 P99/Max에서 터집니다.
- **workers**: 서버 프로세스(worker) 수. 모델을 여러 번 로드하게 되어 VRAM 사고를 유발할 수 있음(본 프로젝트는 1 고정 권장)

### 2-4. 멀티 레벨(L1~L5) 자동 선택이 필요한 이유

데이터가 커질수록 Flat 인덱스는 느려지고 메모리 사용이 커집니다.  
따라서 `controller.py`가 DB 데이터 규모를 측정해 `thresholds` 기준으로 **L1~L5 중 최적 레벨을 자동 선택**합니다.

---

## 3) 권장 실행 환경(검증 환경 포함)

### 3-1. 일반 권장(범용)

- Windows 11 + WSL2
- Ubuntu 22.04/24.04 LTS
- Python 3.12+(venv)
- SSD 권장(인덱스/모델 다운로드)
- NVIDIA GPU 권장(임베딩/리랭킹 가속)

### 3-2. 검증된(사용자) 환경(참고)

- CPU: AMD Ryzen 7 5800XT (8‑Core)
- RAM: 32 GB
- SSD: Samsung 990Pro (M.2)
- GPU: NVIDIA GeForce RTX 3060 Ti (8GB VRAM)
- CUDA Toolkit: 13.1
- Python(venv): 3.12.3
- Ubuntu(WSL): 24.04.4 LTS (noble) / WSL2

### 3-3. GPU/WSL 사전 확인(꼭 한번)

```bash
nvidia-smi
python -c "import torch; print(torch.cuda.is_available(), torch.version.cuda)"
```

- `torch.cuda.is_available()`가 False면, GPU 루트 설치를 해도 CPU로 동작할 수 있습니다.

---

## 4) 최신 파일 구조 & 생성되는 파일

### 4-1. 파일 트리(현재 기준)

```text
RAG/
├─ data/
│  ├─ categories.json
│  └─ table_map.json
├─ L1_Flat/ ... L5_Milvus/
│  ├─ app.py
│  └─ db_sync.py
├─ L6_Elasticsearch/
│  ├─ app.py
│  └─ product_embedder.py
├─ controller.py
├─ rag_config.yml
├─ requirements.txt
├─ stress_test.py
└─ rag_test_report_YYYYMMDD_HHMMSS.txt   # (생성됨)
```

### 4-2. 실행하면서 생성될 수 있는 파일(대표)

`rag_config.yml`의 `paths` 설정에 따라 다음이 생성됩니다(상대경로 주의).

- 인덱스 파일/폴더: `paths.index_root`
- 메타 파일: `paths.meta_file`
- 동기화 상태 파일: `paths.state_file`
- 로그 파일: `paths.log_path`

> 🔎 “상대경로 주의”  
> 레벨 스크립트는 **레벨 폴더를 cwd로 실행**될 수 있습니다.  
> 따라서 `./data/...`는 “레벨 폴더 내부”에 생길 수 있습니다(정상).

---

## 5) 처음 실행(최초 1회) — WSL 설치부터 완전 상세

### 5-1. Windows PowerShell에서 WSL2 설치/검증

```powershell
wsl --install
wsl -l -v
wsl -d Ubuntu
```

- Ubuntu가 `VERSION 2`인지 확인합니다.
- 만약 VERSION 1이면:

```powershell
wsl --set-default-version 2
```

### 5-2. Ubuntu(WSL) 패키지 설치

```bash
sudo apt update
sudo apt install -y git curl build-essential python3-pip python3-venv
python3 -V
pip3 -V
```

### 5-3. 프로젝트 폴더로 이동

```bash
cd /mnt/c/rag/service   # 본인 경로로 변경
```

> ⚠️ 성능 팁  
> `/mnt/c`는 Windows 파일시스템을 경유합니다. 대규모 인덱스 I/O가 많다면 WSL 홈으로 옮기는 것도 고려하세요.
>
> ```bash
> mkdir -p ~/rag && cp -r /mnt/c/rag/service ~/rag/service && cd ~/rag/service
> ```

### 5-4. venv 생성/활성화(정석)

```bash
python3 -m venv venv
source venv/bin/activate
which python
python -V
python -m pip -V
python -m pip install -U pip setuptools wheel
```

---

## 6) venv 사용 원칙(실수 방지)

### 6-1. “pip install은 언제 해야 하나?”

✅ 정석: **venv 활성화 후 설치**  
활성화 없이 설치하면 전역 Python에 들어가 충돌/재현성 문제가 생깁니다.

### 6-2. 실수 방지 패턴

- 항상 `python -m pip ...` 형태로 설치하세요.
- `pip ...` 단독 사용은 다른 python을 가리킬 수 있습니다.

---

## 7) 의존성 설치 — A/B/C 옵션 + CPU/GPU 분기(상세)

> 여기서부터가 “설치 성공률”을 결정합니다.  
> **A안(권장) → 실패 시 B안(디버깅) → 운영 안정성 필요하면 C안(Conda)** 순으로 선택하세요.

### 7-0. 공통 준비(필수)

```bash
source venv/bin/activate
python -m pip install -U pip setuptools wheel
```

---

### 7-A) ✅ A안(권장): requirements.txt로 설치(표준 절차)

#### 7-A-1) GPU 환경(권장)

PyTorch CUDA 휠이 필요할 수 있으므로, 아래 설치를 표준으로 권장합니다.

```bash
python -m pip install -r requirements.txt --extra-index-url https://download.pytorch.org/whl/cu126
```

설치 확인:

```bash
python -c "import torch; print('torch', torch.__version__, 'cuda?', torch.cuda.is_available(), 'cuda_ver', torch.version.cuda)"
python -c "import faiss; print('faiss OK')"
nvidia-smi
```

#### 7-A-2) CPU 환경(서버에 GPU가 없을 때)

```bash
python -m pip install -r requirements.txt
```

그리고 `rag_config.yml`에서:

- `model.device: "cpu"`

---

### 7-B) ✅ B안(디버깅용): 한 줄에 하나씩 설치(실패 지점 추적)

한 번에 여러 패키지 설치 시 중간 실패가 나면, 앞쪽 패키지까지 “설치 안 된 것처럼” 보일 수 있습니다.
이때는 1개씩 설치해서 실패 지점을 정확히 잡습니다.

#### 7-B-1) 서버/유틸(필수)

```bash
python -m pip install pyyaml
python -m pip install pydantic
python -m pip install fastapi
python -m pip install uvicorn
python -m pip install hypercorn
python -m pip install apscheduler
python -m pip install rich
python -m pip install tqdm
python -m pip install requests
python -m pip install pymysql
python -m pip install loguru
python -m pip install psutil
python -m pip install pynvml
```

#### 7-B-2) 모델/추론(필수)

```bash
python -m pip install sentence-transformers
python -m pip install transformers
python -m pip install safetensors
```

#### 7-B-3) Torch: CPU vs GPU 선택

- GPU(CUDA) 예시:

```bash
python -m pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu126
```

- CPU:

```bash
python -m pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu
```

#### 7-B-4) FAISS: CPU vs GPU 선택

- 안정 1순위(권장): CPU FAISS

```bash
python -m pip install faiss-cpu
```

- CUDA 12 GPU FAISS(환경에 따라 성공/실패 가능):

```bash
python -m pip install faiss-gpu-cu12
```

> ❌ `faiss-gpu`는 pip에서 “그 이름 그대로” 배포가 없는 경우가 많습니다.

#### 7-B-5) 테스트용(부하테스트 실행 시)

```bash
python -m pip install aiohttp
```

---

### 7-C) ✅ C안(가장 안정적인 GPU‑FAISS/기업용): Conda/Miniforge 루트

GPU‑FAISS는 pip에서 환경 의존성이 까다로운 경우가 많습니다.
운영/기업 환경에서 안정성을 우선할 때 conda‑forge 루트가 더 안정적인 경우가 있습니다.

- (개요) Miniforge 설치 → conda env 생성 → 설치

```bash
conda create -n rag python=3.12 -y
conda activate rag
conda install -c conda-forge faiss-gpu -y
pip install -r requirements.txt --extra-index-url https://download.pytorch.org/whl/cu126
```

> conda를 쓰면 venv 대신 conda env를 표준으로 삼는 편이 자연스럽습니다.  
> 팀 정책에 따라 “pip(venv)” 또는 “conda(env)” 중 하나로 통일하세요.

---

## 8) 설치 검증 체크리스트(실행 전에 무조건 통과)

```bash
python -c "import yaml; print('pyyaml OK')"
python -c "import fastapi; print('fastapi OK')"
python -c "import uvicorn; print('uvicorn OK')"
python -c "import hypercorn; print('hypercorn OK')"
python -c "import pymysql; print('pymysql OK')"
python -c "import faiss; print('faiss OK')"
python -c "import torch; print('torch OK', torch.__version__, 'cuda?', torch.cuda.is_available(), 'cuda_ver', torch.version.cuda)"
python -c "from sentence_transformers import SentenceTransformer; print('sentence-transformers OK')"
```

---

## 9) rag_config.yml — 섹션별 의미/튜닝 가이드(상세)

`rag_config.yml`은 시스템 전체(L1~L6)가 참조하는 공통 설정입니다.

### 9-1) database (필수)

- DB 접속 정보 및 동기화 대상 테이블 목록
- 테이블이 추가되면 `tables`에 반영해야 인덱싱 대상에 포함됩니다.

### 9-2) model (필수)

- embedding: `BAAI/bge-m3`
- reranker: `BAAI/bge-reranker-v2-m3`
- device: `cuda` / `cpu`

### 9-3) paths (중요)

- 인덱스/메타/상태/로그의 저장 위치
- 상대경로(`./...`)는 실행 cwd 기준으로 생성될 수 있습니다.

### 9-4) thresholds (레벨 자동 선택 기준)

- 총 데이터 건수 기준으로 L1~L5 선택
- 너무 낮게 잡으면 잦은 레벨 교체가 발생할 수 있고, 너무 높게 잡으면 성능 병목이 생길 수 있습니다.

### 9-5) search (품질/속도 튜닝)

- `top_k_retrieval`: 1차 검색 후보 수(↑이면 품질↑, 비용↑)
- `top_k_final`: 최종 반환 수
- `score_threshold`: 너무 낮은 결과 제거

### 9-6) index_params (FAISS 튜닝)

- HNSW `hnsw_m`: 연결 밀도(↑ 정확/메모리↑)
- IVF `ivf_nprobe`: 탐색 클러스터 수(↑ 정확/지연↑)
- PQ `pq_m`: 압축 분할(↑ 정확/메모리↑)

---

## 10) 실행 방법(권장/레벨 단독/장기 실행)

### 10-1) 권장 실행(전체 자동 운영)

```bash
source venv/bin/activate
python controller.py
```

### 10-2) 실행 직후 확인(포트)

```bash
ss -lntp | grep -E "8077|8078"
```

### 10-3) 특정 레벨만 디버깅

```bash
cd L2_HNSW
source ../venv/bin/activate
python db_sync.py
python app.py
```

### 10-4) WSL에서 장기 실행(터미널 닫아도 유지): tmux 권장

```bash
sudo apt install -y tmux
tmux new -s rag
source venv/bin/activate
python controller.py
# detach: Ctrl+B, D
# attach: tmux attach -t rag
```

---

## 11) API 사용 예시(`/search`, `/sync-item`, `/embed-query`)

### 11-1) 메인 엔진(로컬): /search

`stress_test.py`가 사용하는 기본 payload는 다음 형태입니다.

```json
{ "q": "소가죽 스웨터 찾아줘" }
```

curl 예시:

```bash
curl -X POST "http://localhost:8077/search"   -H "Content-Type: application/json"   -d '{"q":"소가죽 스웨터 찾아줘"}'
```

> 선택 파라미터(구현에 따라 지원): `top_k` 등  
> (지원 여부는 각 레벨 app.py를 확인하세요.)

### 11-2) 실시간 개별 동기화: /sync-item/{source_id}

controller.py는 L6와 엔진 양쪽에 동기화를 동시에 트리거합니다.

```bash
curl -X POST "http://localhost:8077/sync-item/Products:105"
curl -X POST "http://localhost:8078/sync-item/Products:105"
```

### 11-3) L6 임베딩: /embed-query

```bash
curl -X POST "http://localhost:8078/embed-query"   -H "Content-Type: application/json"   -d '{"q":"소가죽 스웨터 찾아줘"}'
```

---

## 12) 운영 안전장치(필수)

### 12-1) ⚠️ workers=1 고정(VRAM/OOM 셧다운 방지)

- 워커를 늘리면 모델이 프로세스마다 중복 로드되어 VRAM이 급증합니다.
- 8GB VRAM 환경에서는 특히 위험합니다.

✅ 운영 룰:

- 각 레벨 `app.py` 실행 설정은 **workers=1 고정**
- 성능 개선은 워커 증가가 아니라:
  - micro‑batching, Top‑K 조정, 캐시, 인덱스 파라미터 튜닝으로 접근

### 12-2) 폭격/스파이크 대비(Backpressure 필요)

stress test에서 확인되듯, 동시 폭격/스파이크에서는 지연이 연쇄로 커집니다.

- In‑Flight 제한(동시 처리 제한)
- 429(Too Many Requests)로 빠른 거절
- 큐 길이 제한/서킷 브레이커(부하 시 reranker skip 등)

---

## 13) Reset/복구(가벼운 리셋 → 완전 초기화)

### 13-1) 포트 점유 프로세스 정리

```bash
ss -lntp | grep -E "8077|8078"
kill -9 <PID>
```

### 13-2) venv 완전 초기화(의존성 꼬임 해결)

```bash
deactivate 2>/dev/null || true
rm -rf venv
python3 -m venv venv
source venv/bin/activate
python -m pip install -U pip setuptools wheel
python -m pip install -r requirements.txt --extra-index-url https://download.pytorch.org/whl/cu126
```

---

## 14) Stress Test — 코드/데이터/재현/리포트 해석(상세)

### 14-1) 테스트 코드/데이터(정의)

- 테스트 코드: `stress_test.py`
- 테스트 데이터(산출물): `rag_test_report_YYYYMMDD_HHMMSS.txt` (자동 생성)

### 14-2) 테스트가 치는 엔드포인트(마스킹 표기)

- SEARCH: `http://DDNS:포트번호/search`
- SYNC‑ITEM: `http://DDNS:포트번호/sync-item/{source_id}`
- L6_EMBED: `http://DDNS:포트번호/embed-query`

### 14-3) 실행 방법(재현)

```bash
source venv/bin/activate
python -m pip install aiohttp
python stress_test.py
```

### 14-4) 최신 리포트 요약(2026‑02‑27)

기준 파일: `rag_test_report_20260227_050001.txt`

- 전체(9,000건):
  - SLA 성공(≤3s): 5,787 (64.30%)
  - SLA 지연(>3s): 3,196 (35.51%)
  - 실패: 17 (0.19%)

- 타입별:
  - L6_EMBED: 5,135/6,233 (82.38%) / 지연 1,093 / 실패 5
  - SEARCH : 117/1,846 (6.34%) / 지연 1,720 / 실패 9
  - SYNC : 535/921 (58.09%) / 지연 383 / 실패 3

모드별 집계:
| 모드 | 총 요청 | SLA 성공률(평균) | SLA 성공률(최저~최고) | 최악 P99(s) | 최악 Max(s) | 실패 합계 |
|---|---:|---:|---:|---:|---:|---:|
| 모드 1: 동시 폭격 | 1,000 | 36.20% | 28.00% ~ 39.50% | 14.782 | 15.350 | 9 |
| 모드 2: 릴레이 요청 | 1,000 | 80.80% | 75.50% ~ 92.50% | 9.774 | 10.301 | 0 |
| 모드 3: 그룹 파도타기 | 1,500 | 68.73% | 61.33% ~ 73.33% | 15.095 | 15.098 | 2 |
| 모드 4: 점진적 부하 증가 | 1,500 | 77.67% | 74.33% ~ 79.67% | 7.729 | 7.779 | 1 |
| 모드 5: 극한 피크 시뮬레이션 | 4,000 | 60.53% | 53.75% ~ 68.50% | 33.771 | 35.510 | 5 |

에러 패턴(상위):

- [WinError 10054] An existing connection was forcibly closed by the remote host × 4
- Server disconnected × 2

해석(운영 관점):

- 모드1(동시 폭격)과 모드5(스파이크)에서 P99/Max가 크게 증가합니다(큐잉/자원 고갈 신호).
- 실패는 많지 않더라도, “지연”이 많으면 사용자 체감상 장애에 가깝습니다.
- 우선순위는 Backpressure(429), In‑Flight 제한, 부하 시 다운그레이드(예: reranker skip)입니다.

---

## 15) License (상세)

이 프로젝트는 **소스코드**, **제3자 라이브러리/모델(가중치)**, **DB 데이터(콘텐츠)**, **로그/리포트(운영 산출물)**이
서로 다른 권리/라이선스/보안 규칙을 가질 수 있습니다.

- 소스코드: MIT License(본 저장소의 코드)
- 제3자 구성요소: 각 라이선스 별도 적용(모델 가중치 포함)
- DB 데이터: 코드 라이선스와 분리(조직/운영 정책)
- 로그/리포트: 외부 공유 시 마스킹 필수

의존성 라이선스 목록 생성(권장):

```bash
source venv/bin/activate
python -m pip install pip-licenses
pip-licenses --format=markdown --with-authors --with-urls > THIRD_PARTY_NOTICES.md
```

---
