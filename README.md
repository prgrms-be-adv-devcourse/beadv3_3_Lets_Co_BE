# 🙋‍♂️ 개인 제작 상품 판매 사이트 개발 프로젝트

## ❗팀 소개

### ℹ️ 팀 이름 : Let's Co

| 문민규                                                                            | 정운석                                                                             | 이용희                                                                                 | 안수현                                                                                  | 최정민                                                                             |
|:-------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------:|
| <img src=""> | <img src=""> | <img src=""> | <img src=""> | <img src=""> |
| PO/BE                                  | BE                                           | BE/DevOps                                          | BE/DB                                                                            | BE                                             |
| [GitHub]()                                                               | [GitHub]()                                                                 | [GitHub]()                                                                     | [GitHub]()                                                              | [GitHub]()                                                                     |

<details>
<summary><strong>👥 팀원별 작업</strong></summary>
  
### 문민규 (PO / Backend)
> **핵심 역할: 상품 리뷰 시스템, 전략 및 로드맵 수립, 각 협업 페이지 관리 및 운영**

**상품 리뷰 시스템**
- 작성 예정

**전략 및 로드맵 수립**
- 작성 예정

**각 협업 페이지 관리 및 운영**
- 작성 예정

---

### 정운석 (Backend)
> **핵심 역할: 주문/장바구니 도메인 구축, 테스트 코드 작성**

**주문 도메인**
- 장바구니 기반 주문 생성 로직
- 주문 상태 관리
- 구매자/판매자별 주문 목록 조회 API 개발
  
**장바구니 시스템**
- 장바구니 CRUD 및 상품 선택/해제 기능 구현
- 결제 대상 상품 관리 로직 개발
- Kafka 이벤트 기반 재고 처리 로직 구현

**테스트 코드**
- 각 주요 기능 테스트 코드 구현
- 

---

### 이용희 (Backend / DevOps)
> **핵심 역할: 상품/게시판 도메인 및 Elastic Search기반 검색, 인프라(K3S/CI/CD 구축**

**상품 Seller, Admin Service**
- **상품 CRUD API**: Seller 본인 상품 조회/등록/수정/삭제 (필터링, 페이징)
- **상품 Admin API**: 상품 목록 조회/수정/제거 (필터링, 페이징)

**게시판 도메인**
- **Q&A CRUD API**: 관리자, 상품 문의 등록/수정/삭제/조회(상세조회, 목록조회)
- **관리자 공지 CRUD API**: Admin 공지 작성/수정/삭제/상세조회
- **문의 관련 API**: 문의 조회/답변 등록/삭제
- **공지 관련 API**: 공지 목록 조회, 상세 조회

**검색 시스템 (Elasticsearch)**
- **통합 검색**: 복합 필터(이미지, 카테고리)를 지원하는 고성능 검색 API 구현
- **추천 알고리즘**:
    - **알고리즘1**:
    - **알고리즘2**:
    - **알고리즘3**:

**DevOps 인프라**
- **K3S(K8S 경량화 버전)** 클러스터 구축 및 서비스별 Mainfest 관리
- **GitHub Actions** 연동 자동화 CI/CD 파이프라인 구축
- Docker Compose 기반 로컬 개발 환경 표준화

**Swagger, Zipkin**
- **Swagger** 기반 API 문서화 작업
- **Zipkin** 도입으로 시스템 흐름 파악 크게 향상
- 협업시 프론트엔드와 커뮤니케이션 비용 감소, 개발 생산성 크게 증가

---

### 안수현 (Backend / Database)
> **핵심 역할: 사용자 도메인/DB 구축, MSA 아키텍처 설계**

**사용자 도메인**
- 작성 예정

**Database 구축 및 운영**
- 작성 예정

**아키텍처 확장**
- Eureka 서비스 디스커버리 구성
- Spirng Cloud Gateway 구성
- AI 서비스 모듈 구성 및 통합

---

### 최정민 (Backend)
> **핵심 역할: 결제/예치금 시스템 구축 및 Spring Batch 정산 시스템 구축**

**결제 시스템**
- 작성 예정

**예치금(deposit) 시스템**
- 작성 예정

**Spring Batch 정산 시스템**
- Chunk 방식 배치 처리
- 실패 정산 자동 재시도 배치 스케줄러 구현

</details>

---

## ❗ 사이트 소개

<img src= "" />

### ℹ️ 사이트 이름 : GutJJeu
> **세상에 없던 나만의 굿즈 판매 이커머스 플랫폼**

> 예치금 시스템과 나에게 맞는 상품 추천 시스템을 갖춘 안전한 거래 환경 제공

> Spring Cloud MSA + Kafka + Elasticsearch + AI + Toss Payments 기반 실무형 플랫폼

---

### 🎬 화면

---

### 📪 링크
배포 링크 참조 예정

---

## ❗개발

### 🔊주요 기능


<details>
<summary><h3>🔐 User Service - 회원 및 인증</h3></summary>

#### 인증 (Auth)
- OAuth 2.0 Google 로그인 (isNewUser 분기 처리)
- Access Token 재발급 (Refresh Token 기반)
- 로그아웃 (Redis Token 삭제 + Cookie 만료)
- JWT 토큰 관리 (HttpOnly Cookie 방식)

#### 회원 (User)
- 회원가입 (가입 시 토큰 자동 발급)
- 회원정보 조회 (내 정보 / 특정 사용자)
- 회원정보 수정 (닉네임, 전화번호, 주소)
- 프로필 이미지 관리 (S3 업로드/교체)

#### 판매자 권한
- SMS 인증 코드 발송 (6자리)
- 인증 코드 검증 후 SELLER 권한 부여

</details>


<details>
<summary><h3>🎁 Product Service - 상품</h3></summary>
- 

</details>


<details>
<summary><h3>📞 Order Service - 주문, 장바구니</h3></summary>
- 
  
</details>


<details>
<summary><h3>💸 Payment Service - 결제, 정산</h3></summary>
- 

</details>


<details>
<summary><h3>⁉️ Board Service - Q&A, 공지</h3></summary>
- 
  
</details>


<details>
<summary><h3>🤖 AI Service - 상품 추천</h3></summary>
- 

</details>

---

### 📆 개발 기간

---

### 🖥 System 아키텍쳐

---

### 🖥 ERD Diagram

---

### 🛠 기술 스택

#### 💻 Language
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)

#### ⚙ Framework & Library
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/SpringSecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/SpringDataJPA-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white)
![Spring Batch](https://img.shields.io/badge/SpringBatch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring AI](https://img.shields.io/badge/SpringAI-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Google OAuth](https://img.shields.io/badge/Google_OAuth2.0-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Toss Payments](https://img.shields.io/badge/Toss_Payment-FF3B30?style=for-the-badge&logoColor=white)

![OpenFeign](https://img.shields.io/badge/OpenFeign-00CCFF?style=for-the-badge&logo=apache-feign&logoColor=white)

#### 🗄 Database & Search

![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)


![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Logstash](https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=logstash&logoColor=white)
![Kibana](https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white)

#### 🛠 Infra
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

![AWS ECS](https://img.shields.io/badge/AWS%20ECS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Amazon S3](https://img.shields.io/badge/AmazonS3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![Nginx Proxy Manager](https://img.shields.io/badge/Nginx_Proxy_Manager-009639?style=for-the-badge&logo=nginx&logoColor=white)

#### 🌐 MSA & Messaging
![Eureka](https://img.shields.io/badge/Eureka-0061A8?style=for-the-badge&logo=netflix&logoColor=white)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud_Gateway-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)

#### 🔧 Collaboration Tools
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Zep](https://img.shields.io/badge/Zep-008080?style=for-the-badge&logo=artstation&logoColor=white)
![ERDCloud](https://img.shields.io/badge/ERDCloud-4A90E2?style=for-the-badge&logo=icloud&logoColor=white)

---
