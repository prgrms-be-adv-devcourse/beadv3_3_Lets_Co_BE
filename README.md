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
- 작성 예정

**장바구니 도메인**
- 작성 예정

**테스트 코드**
- 작성 예정

---

### 이용희 (Backend / DevOps)
> **핵심 역할: 상품/게시판 도메인 및 Elastic Search기반 검색, 인프라(K3S/CI/CD 구축**

**상품 도메인**
- 작성 예정

**게시판 도메인**
- 작성 예정

**검색 시스템 (Elasticsearch)**
- 작성 예정

**DevOps 인프라**
- 작성 예정

---

### 안수현 (Backend / Database)
> **핵심 역할: 사용자 도메인/DB 구축, MSA 아키텍처 설계**

**사용자 도메인**
- 작성 예정

**Database 구축 및 운영**
- 작성 예정

**아키텍처 확장**
- 작성 예정

---

### 최정민 (Backend)
> **핵심 역할: 결제/예치금 시스템 구축 및 Spring Batch 정산 시스템 구축**

**결제 시스템**
- 작성 예정

**예치금(deposit) 시스템**
- 작성 예정

**Spring Batch 정산 시스템**
- 작성 예정

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

### 📜 Project Docs

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

![QueryDSL](https://img.shields.io/badge/QueryDSL-FF5722?style=for-the-badge)
![OpenFeign](https://img.shields.io/badge/OpenFeign-00CCFF?style=for-the-badge&logo=apache-feign&logoColor=white)

#### 🗄 Database & Search
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![H2](https://img.shields.io/badge/H2-00599C?style=for-the-badge&logo=h2&logoColor=white)

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
![Kafka UI](https://img.shields.io/badge/Kafka_UI-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)

#### 🔧 Collaboration Tools
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Zep](https://img.shields.io/badge/Zep-008080?style=for-the-badge&logo=artstation&logoColor=white)

![Canva](https://img.shields.io/badge/Canva-00C4CC?style=for-the-badge&logo=canva&logoColor=white)
![ERDCloud](https://img.shields.io/badge/ERDCloud-4A90E2?style=for-the-badge&logo=icloud&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)

---
