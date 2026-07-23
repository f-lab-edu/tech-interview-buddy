# 채용공고 기능 설계

## 확정된 방향

- 사용자 유형: 일반 사용자(조회·지원), 채용담당자(게시·지원자 관리), 관리자(승인·운영)
- 채용담당자 계정은 **관리자 승인제**
- **지원(apply) 기능 포함** — 기존 Resume를 첨부해 지원
- 공고 기술스택은 **기존 Tag와 공유** (면접 질문 연계의 기반)
- **Company 엔티티 도입** (담당자는 회사 소속, 공고는 회사 명의)
- 향후 네트워킹 확장(프로필, 팔로우, 메시징)을 막지 않는 구조로 설계

---

## 1. 도메인 모델

### 신규 엔티티

**Company** — 기업

| 필드 | 타입 | 비고 |
|------|------|------|
| id | Long | PK |
| name | String | 유니크 |
| description | Text | nullable, 회사 소개 |
| createdAt/updatedAt | | BaseEntity 패턴 |

**RecruiterRequest** — 담당자 승인 신청

| 필드 | 타입 | 비고 |
|------|------|------|
| id | Long | PK |
| user | User FK | 신청자 |
| companyName | String | 신청 시 입력 (승인 시 Company 생성 or 기존 연결) |
| contactInfo | String | 검증용 (회사 이메일 등) |
| status | Enum | PENDING / APPROVED / REJECTED |
| processedAt | LocalDateTime | nullable |

승인 시: `user.role = RECRUITER`, `user.company` 연결. 같은 회사가 이미 있으면 기존
Company에 연결 (한 회사 여러 담당자 지원).

**JobPosting** — 채용공고

| 필드 | 타입 | 비고 |
|------|------|------|
| id | Long | PK |
| company | Company FK | 공고 명의 |
| author | User FK | 작성 담당자 |
| title | String | |
| position | String | 직무 (예: 백엔드 개발자) |
| employmentType | Enum | FULL_TIME / CONTRACT / INTERN |
| careerLevel | Enum | ENTRY / JUNIOR / SENIOR / ANY |
| region | String | MVP는 문자열, 추후 Enum/코드화 |
| salaryText | String | nullable ("협의" 허용을 위해 텍스트) |
| content | Text | 상세 내용 |
| deadline | LocalDate | nullable (상시 채용) |
| status | Enum | DRAFT / PUBLISHED / CLOSED / DELETED |
| timestamps | | |

**JobPostingTag** — 공고 ↔ Tag 연결 (기존 `QuestionTag`와 동일 패턴)

**JobApplication** — 지원

| 필드 | 타입 | 비고 |
|------|------|------|
| id | Long | PK |
| jobPosting | JobPosting FK | |
| applicant | User FK | |
| resume | Resume FK | 지원 시 선택한 이력서 |
| status | Enum | SUBMITTED / UNDER_REVIEW / ACCEPTED / REJECTED / WITHDRAWN |
| appliedAt / processedAt | | |

제약: `unique(job_posting_id, applicant_id)` — 공고당 1회 지원.
WITHDRAWN 후 재지원 허용 여부는 정책 결정 필요 (기본: 불허가 단순).

### 기존 엔티티 변경

- **UserRole**: `RECRUITER` 추가 (USER / RECRUITER / ADMIN)
- **User**: `company` FK 추가 (nullable, RECRUITER만 사용)
- **Notification**: type 확장 — 지원 접수(담당자에게), 지원 상태 변경(지원자에게),
  공고 마감 임박(선택). 기존 이력서 분석 알림과 같은 테이블에 type 필드로 구분

---

## 2. 핵심 설계 결정

### 이력서 열람 권한 (가장 민감한 부분)

지원 = "이 공고의 담당자에게 이 이력서 열람을 허용한다"는 **명시적 동의**로 정의한다.

- 담당자는 **자기 회사 공고에 접수된 지원의 이력서만** 열람 가능
- 구현: `ResumeAccessValidator`에 열람 경로 추가
  - 기존: `getOwnedResume(resumeId, userId)` — 본인 소유
  - 신규: `getResumeForApplication(applicationId, recruiterUserId)` —
    지원 건을 통해서만 접근, 담당자의 회사와 공고의 회사 일치 검증
- 담당자가 이력서를 검색/브라우징하는 기능은 **없음** (네트워킹 단계에서 프로필
  공개 동의와 함께 별도 설계)
- **이력서 열람 감사 로그** (`ResumeAccessLog`): 담당자가 지원 이력서를 열 때마다
  `(열람자, 지원 건, 열람 시각)`을 기록. 유출 사고 시 경로 추적, 분쟁 대응,
  이상 열람 탐지의 근거. 열람 API에서 저장. Phase 3 필수.

### 회사 사칭 방지 (민감)

`companyName`은 사용자 자유 입력이고 승인 시 같은 이름이면 기존 Company에 합류하므로,
"카카오" 같은 이름으로 신청→승인되면 그 회사 지원자 이력서 전체에 접근하게 된다.
회사 단위로 지원 데이터가 공유되기 때문(권한 규칙 = 담당자 company == 공고 company).

- **MVP**: 어드민 승인 화면에 경고 — "이 회사에 이미 담당자 N명 있음 / 기존 담당자: OOO".
  승인 전 확인을 강제. 코드 변경 최소.
- **회사명 정규화**: `findByName` 전에 `trim()` + 표기 정규화. 미적용 시 "카카오",
  "(주)카카오", "카카오 " 가 별개 회사로 쪼개져 같은 회사 담당자끼리 서로 못 봄.
  승인 시 어드민이 기존 회사 목록에서 선택(자동완성)하는 UI 권장.
- **향후**: 회사 이메일 도메인 검증(`Company.emailDomain` + `contactInfo` 도메인 일치),
  또는 첫 담당자만 어드민 승인·이후는 기존 담당자 초대 방식.

### 담당자 권한 회수

승인의 역방향이 필요하다. 퇴사자가 계속 지원자 이력서를 보거나 오승인을 되돌릴 수 없으면 곤란.

- 어드민 API `revokeRecruiter(userId)`: `role = USER`, `company = null`.
- `User.demoteToUser()` 메서드 + 어드민 엔드포인트 하나. Phase 1에 포함.

### 지원 후 이력서 수정/삭제 문제

지원에 연결된 이력서를 사용자가 삭제하면 담당자가 볼 수 없게 된다. 선택지:

- (A) **활성 지원(SUBMITTED/UNDER_REVIEW)이 있는 이력서는 삭제 차단** ← MVP 채택
- (B) 지원 시점에 S3 파일 스냅샷 복사 — 정확하지만 구현·스토리지 비용

(A)로 시작: `ResumeDeleteService`에 활성 지원 존재 검사 추가,
"진행 중인 지원이 있는 이력서는 삭제할 수 없습니다" (409 Conflict).

**주의 — 마감 공고로 인한 이력서 영구 잠김**: 공고가 CLOSED 되어도 지원 상태는
SUBMITTED로 남는다. 담당자가 처리를 안 하면 지원자는 자기 이력서를 영영 삭제 못 한다
(개인정보 파기 불가). 대응:
- 삭제 차단 조건을 "활성 지원 **그리고** 공고도 활성"으로 좁힘, 또는
- 공고 CLOSED 시 미처리 지원을 자동 종결(EXPIRED 등), 또는
- 최소한 삭제 실패 메시지에 "철회(WITHDRAWN) 후 삭제 가능"을 안내하고,
  지원자의 철회 경로를 항상 열어둠.
MVP는 안내 문구, 정식 대응은 자동 종결.

### 공고 상태 라이프사이클

```
DRAFT ──게시──▶ PUBLISHED ──마감──▶ CLOSED
                    │                  ▲
                    └── deadline 경과 ──┘ (스케줄러, @Scheduled 일 1회)
소프트 삭제: 어느 상태에서든 DELETED (목록/검색에서 제외, 데이터 보존)
```

- CLOSED/DELETED 공고에는 지원 불가
- 지원이 존재하는 공고는 하드 삭제 금지 (지원 이력 보존)

### 지원 상태 라이프사이클

```
SUBMITTED ──담당자──▶ UNDER_REVIEW ──▶ ACCEPTED / REJECTED
    │
    └──지원자──▶ WITHDRAWN (SUBMITTED/UNDER_REVIEW에서만 가능)
```

상태 변경 시 상대방에게 Notification 발행.

**알림 폭주 방지**: 인기 공고는 지원이 대량 발생한다. 담당자 대상 "지원 접수" 알림은
건건이 아니라 묶음(예: 일 1회 "새 지원 N건")으로, 지원자 대상 "상태 변경" 알림만 건건.
또는 담당자 알림은 목록 화면 뱃지로 대체.

---

## 3. API 설계

### 공고 (일반)

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| GET | `/api/v1/job-postings` | 전체 | 검색·필터(직무/지역/경력/태그)·페이징 |
| GET | `/api/v1/job-postings/{id}` | 전체 | 상세 |
| GET | `/api/v1/job-postings/{id}/related-questions` | 로그인 | 공고 태그 기반 면접 질문 추천 (recommend 서비스 연계) |

### 공고 (담당자)

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| POST | `/api/v1/job-postings` | RECRUITER | 등록 (자기 회사 명의) |
| PUT | `/api/v1/job-postings/{id}` | RECRUITER | 수정 (자기 회사 공고만) |
| PATCH | `/api/v1/job-postings/{id}/close` | RECRUITER | 수동 마감 |
| DELETE | `/api/v1/job-postings/{id}` | RECRUITER | 소프트 삭제 |

### 지원

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| POST | `/api/v1/job-postings/{id}/applications` | USER | 지원 (body: resumeId) |
| GET | `/api/v1/my/applications` | USER | 내 지원 목록·상태 |
| PATCH | `/api/v1/applications/{id}/withdraw` | USER | 지원 철회 (본인만) |
| GET | `/api/v1/job-postings/{id}/applications` | RECRUITER | 지원자 목록 (자기 회사 공고만) |
| GET | `/api/v1/applications/{id}/resume` | RECRUITER | 지원 이력서 열람 (presigned URL) |
| PATCH | `/api/v1/applications/{id}/status` | RECRUITER | 상태 변경 |

### 담당자 승인

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| POST | `/api/v1/recruiter-requests` | USER | 담당자 신청 |
| GET | `/api/v1/recruiter-requests?status=PENDING` | ADMIN | 대기 목록 |
| PATCH | `/api/v1/recruiter-requests/{id}/approve` | ADMIN | 승인 |
| PATCH | `/api/v1/recruiter-requests/{id}/reject` | ADMIN | 반려 |

### 회사

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| GET | `/api/v1/companies/{id}` | 전체 | 회사 프로필 |
| GET | `/api/v1/companies/{id}/job-postings` | 전체 | 회사의 공고 목록 |
| PUT | `/api/v1/companies/{id}` | RECRUITER | 자기 회사 프로필 수정 |

---

## 4. 권한 규칙 요약

| 행위 | 규칙 | 구현 |
|------|------|------|
| 공고 수정/삭제/마감 | 담당자의 company == 공고의 company | `JobPostingAccessValidator` (ResumeAccessValidator 패턴) |
| 지원자 목록/이력서 열람 | 담당자의 company == 공고의 company | 동일 Validator + Application 경유 검증 |
| 지원 철회 | 본인 지원만 | applicant == currentUser |
| 담당자 승인 | ADMIN만 | `@PreAuthorize("hasRole('ADMIN')")` |
| 회사 프로필 수정 | 소속 담당자만 | user.company == company |

실패 시 기존 도메인 예외 사용: 403 `ResourceAccessDeniedException`,
404 `ResourceNotFoundException`, 409 `DuplicateResourceException`(중복 지원).

---

## 5. 기존 자산 재사용 맵

| 기존 자산 | 재사용처 |
|-----------|----------|
| Tag / QuestionTag 패턴 | JobPostingTag (동일 구조) |
| QueryDSL 검색 (QuestionRepositoryImpl 패턴) | 공고 검색·필터 |
| recommend 서비스 | 공고 태그 → 면접 질문 추천 |
| Notification | 지원/상태변경 알림 (type 확장) |
| Resume + presigned URL | 지원 이력서 열람 |
| ResumeAccessValidator 패턴 | JobPostingAccessValidator |
| 도메인 예외 3종 + GlobalExceptionHandler | 그대로 사용 |
| grant-admin 승인 흐름 | 담당자 승인 UX 참고 |

---

## 6. 구현 순서 (커밋 단위 제안)

**Phase 1 — 기반: 역할과 회사**
1. ✅ `UserRole.RECRUITER` 추가, `Company` 엔티티, `User.company` FK
2. ✅ `RecruiterRequest` + 신청/승인/반려 API (ADMIN)
3. 🔶 시큐리티: `@EnableMethodSecurity` + `@PreAuthorize`로 채용 엔드포인트 전부 보호됨
   (승인/반려/회수=ADMIN, 신청=로그인). 다만 `/api/v1/recruiter-requests/**`는 URL 매처가
   없어 `@PreAuthorize` 한 겹뿐 → **Phase 2에서 URL 규칙과 RECRUITER 전용 매처를
   공고 API와 함께 정리** (매처 순서 주의). 이중 보호로 강화 예정.
4. ✅ `requirePending()` → `InvalidStateException`(409), 어드민 목록 N+1 제거
   (⬜ 승인/반려 알림, ⬜ 회사명 정규화, ⬜ 승인 화면 담당자 경고 — 미완)
5. ✅ 담당자 권한 회수 API (`revokeRecruiter`) + `UserServiceSpec`
6. 🔶 Spock 테스트: `RecruiterRequestServiceSpec`(apply/approve/reject),
   `UserServiceSpec`(회수) 완료. ⬜ 권한 매트릭스 통합 테스트("안 되어야 하는 것") 남음
   + 보너스: `User` 이중 `@Builder` 버그 수정(role 유실)

**Phase 2 — 공고 CRUD와 검색**
4. `JobPosting` + `JobPostingTag` 엔티티, 등록/수정/삭제/마감 API + `JobPostingAccessValidator`
5. 공고 검색 (QueryDSL, 태그 필터 포함) — Question 검색 패턴 이식
6. 마감일 자동 CLOSED 스케줄러 (`@Scheduled`)

**Phase 3 — 지원**
7. `JobApplication` + 지원/철회/내 지원 목록 API (중복 지원 409)
8. 담당자: 지원자 목록, 이력서 열람(권한 검증), 상태 변경 API
9. `ResumeAccessLog` — 이력서 열람 감사 로그 (열람 API에서 기록)
10. `ResumeDeleteService`에 활성 지원 삭제 차단 + 마감 공고 잠김 대응
11. 지원 관련 Notification 연동 (묶음 알림 정책 적용)

**Phase 4 — 연계 (차별점)**
11. 공고 상세 → 태그 기반 면접 질문 추천 API

각 Phase 완료 시점에 통합 테스트 + `./gradlew build` 확인.
테스트는 기존 컨벤션(Spock, H2, 외부 API mock) 유지.

---

## 7. 이번 범위에서 제외 (네트워킹 단계로 이연)

- 북마크/스크랩, 회사·태그 팔로우
- 사용자 프로필 확장 및 공개 범위 설정
- 담당자의 인재 검색/브라우징 (프로필 공개 동의 설계와 함께)
- 담당자 ↔ 구직자 메시징 (스팸/차단 포함)
- 이력서 ↔ 공고 AI 적합도 분석

## 8. 열어둔 결정 (구현 중 확정)

- WITHDRAWN 후 재지원 허용 여부 (기본안: 불허)
- 비로그인 사용자의 공고 열람 허용 여부 (기본안: 목록/상세 허용, 지원만 로그인)
- 같은 회사 담당자 간 공고 수정 권한 (기본안: 회사 단위 허용 — author 아니어도 가능)
- region 코드화 시점
- 지원/이력서 열람 데이터 보관·파기 기간 (개인정보 — MVP는 정책 문구만, 자동 파기는 이연)
