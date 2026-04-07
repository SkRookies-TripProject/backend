# CosTrip Backend

> **여행 지출 내역 기록 서비스** — REST API 서버  
> SK쉴더스 루키즈 5기 개발4팀

---

## 🌏 프로젝트 소개

CosTrip Backend는 여행별로 예산을 설정하고, 지출 내역을 기록·조회·분석할 수 있는 여행 경비 관리 서비스입니다.

- JWT 기반 무상태(Stateless) 인증으로 보안 처리
- Anthropic Claude API를 활용한 영수증 OCR로 지출 금액 자동 추출
- Flyway를 통한 DB 스키마 버전 관리로 팀 개발 환경 통일
- 모든 응답을 `ApiResponse<T>` 공통 포맷으로 통일하여 프론트엔드 연동 편의성 확보

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.1 |
| Security | Spring Security + JWT (jjwt 0.12.7) |
| ORM | Spring Data JPA / Hibernate |
| DB | MariaDB |
| DB Migration | Flyway |
| HTTP Client | Spring WebFlux (WebClient) |
| External API | Anthropic Claude API (claude-opus-4-5) |

---

## ✨ 핵심 기능

### 1. 인증 (JWT)
- 회원가입 시 비밀번호를 BCrypt로 암호화하여 DB에 저장
- 로그인 성공 시 HS256 알고리즘으로 서명된 JWT 발급 (기본 유효시간 1시간)
- 모든 보호 API는 `Authorization: Bearer <token>` 헤더 필수
- 인증 실패 → 401 JSON, 권한 없음 → 403 JSON 응답 (HTML 리다이렉트 없음)
- `@PreAuthorize("hasRole('ADMIN')")` 으로 관리자 API 접근 제어

### 2. 여행 관리
- 여행 생성 시 국가, 기간, 제목, 카테고리별 예산 함께 등록
- 본인 여행에만 접근 가능 (`trip.userId == 로그인 사용자 id` 검증)
- 여행 삭제 시 하위 지출·후기·첨부파일 CASCADE 삭제

### 3. 지출 관리
- 카테고리(FOOD / TRANSPORT / LODGING / SHOPPING / SIGHTSEEING / OTHER), 금액, 날짜, 메모 기록
- 날짜·카테고리 복합 필터 + 최신순·금액순·날짜순 정렬
- 잔여 예산 = 전체 예산 - 누적 지출 실시간 계산

### 4. 영수증 OCR (Claude API 연동)
- 이미지를 Base64로 수신 → Anthropic Claude claude-opus-4-5 모델에 전달
- 프롬프트로 가게명, 날짜, 항목별 금액, 합계 JSON 추출 요청
- 응답에서 `{` ~ `}` 구간만 추출하여 마크다운 래핑 오류 방지
- 매직 바이트 기반 이미지 포맷 자동 감지 (JPEG / PNG / GIF / WebP)

### 5. 통계
- 카테고리별 금액 합계 및 비율 (소수점 1자리, HALF_UP 반올림)
- 날짜별 지출 합계 및 날짜+카테고리 복합 집계
- 예산 사용률 = (총 지출 / 총 예산) × 100, 예산 미설정 시 0% 처리
- 집계는 JPQL GROUP BY 쿼리로 DB 레벨에서 처리하여 성능 최적화

### 6. 여행 후기
- 여행별 날짜 단위 메모(JournalEntry) 등록·수정·삭제
- 메모당 이미지 다중 업로드 (MultipartFile) → 서버 로컬 저장
- 저장된 이미지는 `/uploads/**` 경로로 브라우저에서 직접 접근 가능

### 7. 관리자 기능
- `ROLE_ADMIN` 권한 보유자만 접근
- KPI 조회: 전체 사용자 수, 총 여행 수, 총 지출 금액
- 통계 조회: Top 5 여행지, 카테고리별 소비 비율
- 사용자 조회·검색(이름·이메일 키워드)·삭제


---

## 🏗 아키텍처

```
Client (React SPA)
        │  HTTP + JWT
        ▼
┌─────────────────────────────────────────────────┐
│                 Spring Boot Server               │
│                                                  │
│  ┌─────────────┐  ┌──────────┐  ┌────────────┐  │
│  │  Security   │  │   API    │  │  Business  │  │
│  │   Layer     │→ │  Layer   │→ │   Layer    │  │
│  │ JWT Filter  │  │Controller│  │  Service   │  │
│  │Spring Sec.  │  │          │  │            │  │
│  └─────────────┘  └──────────┘  └─────┬──────┘  │
│                                        │         │
│                                  ┌─────▼──────┐  │
│                                  │ Data Access│  │
│                                  │ JPA/Hib.   │  │
│                                  └─────┬──────┘  │
└────────────────────────────────────────┼─────────┘
                         ┌──────────────┼──────────────┐
                         ▼              ▼              ▼
                      MariaDB     File Storage    Claude API
                    (4team_db)   (/uploads/**)  (OCR 서비스)
```

---

## 📁 파일 구조

```
backend-dev/
└── src/main/java/com/costrip/costrip_backend/
    │
    ├── auth/                          # 인증 관련
    │   ├── config/
    │   │   ├── SecurityConfig.java    # Spring Security 설정, JWT 필터 등록
    │   │   └── PasswordEncoderConfig.java  # BCrypt 빈 등록
    │   ├── filter/
    │   │   └── JwtAuthenticationFilter.java  # 요청마다 JWT 검증
    │   └── jwt/
    │       ├── JwtService.java        # 토큰 생성·파싱·검증 (HS256)
    │       └── AuthRequest.java
    │
    ├── config/
    │   ├── CorsConfig.java            # CORS 전역 설정 (Security 통합)
    │   └── WebResourceConfig.java     # /uploads/** 정적 리소스 매핑
    │
    ├── controller/                    # API 진입점
    │   ├── AuthController.java        # 회원가입·로그인·로그아웃
    │   ├── TripController.java        # 여행 CRUD
    │   ├── ExpenseController.java     # 지출 CRUD + 필터
    │   ├── ExpenseBudgetController.java  # 예산 조회
    │   ├── StatisticsController.java  # 통계·예산 현황
    │   ├── ReceiptController.java     # 영수증 OCR
    │   ├── AdminController.java       # 관리자 KPI·통계
    │   ├── AdminUserController.java   # 관리자 사용자 관리
    │   ├── UserController.java        # 사용자 정보·비밀번호 변경
    │   └── journal/
    │       ├── JournalEntryController.java       # 여행 메모 CRUD
    │       └── JournalAttachmentController.java  # 메모 이미지 업로드·삭제
    │
    ├── service/                       # 비즈니스 로직
    │   ├── AuthService.java
    │   ├── TripService.java
    │   ├── ExpenseService.java        # 필터·정렬 처리
    │   ├── ExpenseBudgetService.java
    │   ├── StatisticsService.java     # JPQL 집계 + 비율 계산
    │   ├── OcrService.java            # Claude API 호출 + JSON 파싱
    │   ├── AdminService.java
    │   ├── UserService.java
    │   ├── CustomUserDetailsService.java  # Spring Security UserDetails 로드
    │   └── journal/
    │       ├── JournalEntryService.java
    │       └── JournalAttachmentService.java  # 파일 저장·삭제
    │
    ├── entity/                        # JPA 엔티티
    │   ├── User.java
    │   ├── Trip.java
    │   ├── Expense.java
    │   ├── ExpenseBudget.java
    │   ├── Attachment.java
    │   ├── enums/
    │   │   └── ExpenseCategory.java   # FOOD / TRANSPORT / LODGING / SHOPPING / SIGHTSEEING / OTHER
    │   └── journal/
    │       └── JournalEntry.java
    │
    ├── repository/                    # Spring Data JPA Repository
    │   ├── UserRepository.java
    │   ├── TripRepository.java
    │   ├── ExpenseRepository.java     # 집계 JPQL 쿼리 정의
    │   ├── ExpenseBudgetRepository.java
    │   ├── AttachmentRepository.java
    │   └── journal/
    │       └── JournalEntryRepository.java
    │
    ├── dto/                           # 요청·응답 DTO
    │   ├── common/ApiResponse.java    # 공통 응답 래퍼
    │   ├── auth/                      # 로그인·회원가입·관리자 DTO
    │   ├── trip/
    │   ├── expense/
    │   ├── statistics/
    │   ├── receipt/ReceiptDto.java    # OCR 응답 구조
    │   └── journal/
    │
    └── exception/                     # 예외 처리
        └── exception/
            ├── ResourceNotFoundException.java
            └── advice/DefaultExceptionAdvice.java  # 전역 예외 핸들러
│
└── src/main/resources/
    ├── application.yml                # 메인 설정 (DB, JPA, Flyway, JWT, Anthropic)
    ├── application-secret.yml         # 시크릿 값 (git 제외 권장)
    └── db/migration/                  # Flyway 마이그레이션 스크립트
        ├── V1__init.sql               # 기본 테이블 생성 (users, trips, expenses, ...)
        ├── V2__create_journal_tables.sql
        ├── V3__simplify_journal_entries.sql
        └── V4__add_journal_entry_id_to_attachments.sql
```

---

## ▶ 실행 방법

### 사전 요구사항

| 항목 | 버전 |
|------|------|
| Java | 17 이상 |
| Maven | 3.6 이상 |
| MariaDB | 10.6 이상 |


### 환경 변수 설정

`src/main/resources/application-secret.yml` 파일을 생성하거나, 아래 환경 변수를 설정합니다.

```yaml
# application-secret.yml 예시
anthropic:
  api:
    key: sk-ant-xxxxxxxxxxxxxxxxxxxxxxxx
```


### 빌드 및 실행

```bash
# Maven Wrapper 사용
./mvnw spring-boot:run

# 또는 JAR 빌드 후 실행
./mvnw clean package -DskipTests
java -jar target/costrip-backend-0.0.1-SNAPSHOT.jar
```
