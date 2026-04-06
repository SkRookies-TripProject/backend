# Costrip Backend

여행 일정, 지출, 예산, 통계, 여행 일지, 영수증 OCR 기능을 제공하는 Spring Boot 기반 백엔드입니다.  
JWT 인증을 사용하며 MariaDB와 Flyway 마이그레이션을 기준으로 동작합니다.

## 기술 스택

- Java 17
- Spring Boot 3
- Spring Web, Spring WebFlux
- Spring Data JPA
- Spring Security
- JWT (`jjwt`)
- MariaDB
- Flyway
- Lombok
- JUnit 5 / Mockito

## 주요 기능

- 회원가입, 로그인, 로그아웃
- JWT 기반 인증/인가
- 여행 생성, 조회, 수정, 삭제
- 여행별 지출 내역 관리
- 여행별 예산 및 예산 합계 조회
- 여행별 통계 및 예산 요약 조회
- 여행 일지 CRUD
- 여행 일지 첨부파일 업로드/삭제
- 업로드 이미지 정적 서빙 (`/uploads/**`)
- Claude API 기반 영수증 OCR 분석
- 관리자 대시보드 및 사용자 관리

## 주요 API 범위

- 인증: `/api/auth/**`
- 사용자: `/api/users/**`
- 여행: `/api/trips/**`
- 지출: `/api/trips/{tripId}/expenses`, `/api/expenses/{expenseId}`
- 예산: `/api/budgets/**`
- 통계: `/api/trips/{tripId}/statistics`, `/api/trips/{tripId}/budget-summary`
- 여행 일지: `/api/trips/{tripId}/journal-entries`, `/api/journal-entries/{entryId}`
- 첨부파일: `/api/trips/{tripId}/journal-entries/{entryId}/attachments`
- 영수증 OCR: `/api/receipt/analyze`
- 관리자: `/api/admin/**`

## 실행 환경

다음 항목이 준비되어 있어야 합니다.

- JDK 17
- MariaDB 10.x 이상
- Windows 기준 `mvnw.cmd` 또는 Maven 설치 환경

## 환경 변수

`src/main/resources/application.yml` 기준으로 아래 값을 사용할 수 있습니다.

| 변수명 | 기본값 | 설명 |
| --- | --- | --- |
| `DB_HOST` | `25.2.109.64` | MariaDB 호스트 |
| `DB_PORT` | `3306` | MariaDB 포트 |
| `DB_DATABASE` | `4team_db` | 데이터베이스 이름 |
| `DB_USERNAME` | `user` | DB 사용자 |
| `DB_PASSWORD` | `user` | DB 비밀번호 |
| `APP_UPLOAD_DIR` | `uploads` | 업로드 파일 저장 경로 |
| `JWT_SECRET` | 내장 기본값 존재 | JWT 서명 키 |
| `JWT_EXPIRATION` | `3600` | JWT 만료 시간(초) |
| `ANTHROPIC_API_KEY` | 없음 | 영수증 OCR용 Claude API 키 |

## 설정 파일

- `application.yml`: 공통 설정
- `application-secret.yml`: `secret` 프로필용 민감 설정 오버레이

기본 활성 프로필은 `secret`입니다.

## 로컬 실행

### 1. 데이터베이스 준비

애플리케이션 실행 시 Flyway가 아래 마이그레이션을 자동 적용합니다.

- `V1__init.sql`
- `V2__create_journal_tables.sql`
- `V3__simplify_journal_entries.sql`
- `V4__add_journal_entry_id_to_attachments.sql`

### 2. 애플리케이션 실행

PowerShell 기준:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_DATABASE="costrip"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="password"
$env:JWT_SECRET="replace-with-a-secure-secret"
$env:ANTHROPIC_API_KEY="your-api-key"
.\mvnw.cmd spring-boot:run
```

기본 포트는 `8080`입니다.

## 테스트 및 빌드

테스트 실행:

```powershell
.\mvnw.cmd test
```

빌드:

```powershell
.\mvnw.cmd clean package
```

## 인증 방식

- `/api/auth/**`, `/api/receipt/**`, `/uploads/**`는 비인증 접근이 허용됩니다.
- 그 외 대부분의 `/api/**` 요청은 JWT 인증이 필요합니다.
- 인증 정보는 일반적으로 `Authorization: Bearer <token>` 헤더로 전달합니다.

## 업로드 파일

- 업로드 루트는 기본적으로 프로젝트의 `uploads` 디렉터리입니다.
- 업로드된 파일은 `/uploads/**` 경로로 정적 접근할 수 있습니다.
- 여행 썸네일은 첨부파일 경로를 기반으로 응답에 포함됩니다.

## 프로젝트 구조

```text
src
├─ main
│  ├─ java/com/costrip/costrip_backend
│  │  ├─ auth
│  │  ├─ config
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ entity
│  │  ├─ repository
│  │  └─ service
│  └─ resources
│     ├─ application.yml
│     └─ db/migration
└─ test
   ├─ java
   └─ resources
```

## 참고 사항

- `pom.xml`의 `groupId`, `artifactId`, `name`, `description` 메타데이터는 현재 실제 프로젝트명과 다릅니다.
- 영수증 OCR 기능은 외부 API 키가 없으면 정상 동작하지 않습니다.
- 운영 환경에서는 `JWT_SECRET`, DB 계정, 업로드 경로를 기본값 대신 별도 설정으로 분리하는 편이 안전합니다.
