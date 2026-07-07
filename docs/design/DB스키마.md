# DB 스키마 설계

## 개요

- DB: MySQL / MariaDB
- 문자셋: utf8mb4
- 적재 주체: 서버 배치 (초기 수집 + 주기적 동기화)
- 총 8개 테이블

---

## 테이블 목록

| 테이블 | 설명 | 비고 |
|---|---|---|
| `places` | 장소 마스터 | KTO + GWTO 통합 |
| `place_images` | 장소 이미지 | 1:N |
| `place_pet_conditions` | 반려동물 동반 조건 | 1:1 |
| `place_operations` | 운영 정보 | 1:1 |
| `place_accessibility` | 무장애 접근성 | 1:1, fallback 포함 |
| `place_scores` | 펫-워크 지수 | 1:1, 배치 계산 |
| `congestion_forecasts` | 관광지 혼잡도 예측 (30일) | 관광지집중률 API |
| `walk_courses` | 두루누비 산책 코스 | 산책 접근성 계산용 |

---

## DDL

### 1. places — 장소 마스터

```sql
CREATE TABLE places (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    source_type     VARCHAR(10)     NOT NULL,   -- 'KTO' | 'GWTO'
    source_id       VARCHAR(50)     NOT NULL,   -- KTO: contentId, GWTO: contentSeq
    place_type      VARCHAR(20)     NOT NULL,   -- STAY|WORK_PLACE|FOOD|TOUR|WALK|HOSPITAL
    region          VARCHAR(20)     NOT NULL,   -- GANGNEUNG|CHUNCHEON|WONJU
    name            VARCHAR(200)    NOT NULL,
    address         VARCHAR(500),
    latitude        DECIMAL(10, 7),
    longitude       DECIMAL(10, 7),
    tel             VARCHAR(50),
    homepage        VARCHAR(500),
    overview        TEXT,
    last_synced_at  DATETIME,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_source (source_type, source_id),
    INDEX idx_region_type (region, place_type),
    INDEX idx_coords (latitude, longitude)
) CHARACTER SET utf8mb4;
```

**설계 결정:**
- `source_type + source_id` UNIQUE → 중복 적재 방지, 재동기화 시 upsert 기준
- `place_type`은 앱 레이어에서 enum으로 관리
- `last_synced_at`: 동기화 시점 추적, 오래된 데이터 감지용

---

### 2. place_images — 이미지

```sql
CREATE TABLE place_images (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    place_id        BIGINT          NOT NULL,
    image_url       VARCHAR(1000)   NOT NULL,
    is_thumbnail    BOOLEAN         NOT NULL DEFAULT FALSE,
    display_order   INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    INDEX idx_place (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;
```

**설계 결정:**
- KTO `firstimage` / `detailImage2.originimgurl` → is_thumbnail 기준 분리
- GWTO `imageList[].image` → HTTP URL 그대로 저장 (Android에서 cleartext 허용 처리)

---

### 3. place_pet_conditions — 반려동물 동반 조건

```sql
CREATE TABLE place_pet_conditions (
    place_id            BIGINT          NOT NULL,
    acmpy_type          VARCHAR(20),    -- INDOOR|INDOOR_OUTDOOR|OUTDOOR|DESIGNATED|UNKNOWN
    is_cage_required    BOOLEAN         NOT NULL DEFAULT FALSE,
    is_leash_required   BOOLEAN         NOT NULL DEFAULT FALSE,
    pet_weight_limit_kg INT,            -- NULL: 제한 없음
    cat_allowed         BOOLEAN         NOT NULL DEFAULT FALSE,
    bath_available      BOOLEAN         NOT NULL DEFAULT FALSE,
    companion_conditions TEXT,          -- 동반 조건 원문 (acmpyNeedMtr / policyCautions)
    available_facilities TEXT,          -- 이용 가능 시설 (relaPosesFclty / mainFacility)
    cautions            TEXT,           -- 유의사항 (relaAcdntRiskMtr + etcAcmpyInfo)

    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;
```

**소스별 필드 매핑:**

| 컬럼 | KTO 필드 | GWTO 필드 |
|---|---|---|
| `acmpy_type` | `acmpyTypeCd` 코드 변환 | `inOutFlag` (IN→INDOOR, OUT→OUTDOOR, INOUT→INDOOR_OUTDOOR) |
| `is_cage_required` | `acmpyNeedMtr` 텍스트 파싱 | `policyCautions` 텍스트 파싱 |
| `pet_weight_limit_kg` | `acmpyNeedMtr` 텍스트 파싱 | `petWeight` 직접 사용 |
| `cat_allowed` | - | `catFlag` |
| `bath_available` | - | `bathFlag` |
| `companion_conditions` | `acmpyNeedMtr` | `policyCautions` |
| `available_facilities` | `relaPosesFclty`, `relaFrnshPrdlst` | `mainFacility`, `petFacility` |
| `cautions` | `relaAcdntRiskMtr`, `etcAcmpyInfo` | `policyCautions` (통합) |

---

### 4. place_operations — 운영 정보

```sql
CREATE TABLE place_operations (
    place_id            BIGINT          NOT NULL,
    operating_hours     VARCHAR(500),   -- 운영시간 원문
    closed_days         VARCHAR(200),   -- 휴무일 (GWTO는 usedTime 파싱)
    usage_fee           TEXT,           -- 이용요금/입장료
    parking_available   BOOLEAN,        -- NULL: 정보 없음
    indoor_outdoor_type VARCHAR(10),    -- IN|OUT|INOUT (GWTO inOutFlag 동일 값)

    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;
```

**소스별 필드 매핑:**

| 컬럼 | KTO 필드 | GWTO 필드 |
|---|---|---|
| `operating_hours` | `detailIntro2`: `usetime` / `opentimefood` / `checkintime` (타입별) | `usedTime` |
| `closed_days` | `detailIntro2`: `restdate` / `restdatefood` (타입별) | `usedTime` 파싱 (예: "평일 휴무") |
| `usage_fee` | `detailIntro2`: 타입별 상이 | `usedCost` |
| `parking_available` | `detailIntro2`: `parking` / `parkingfood` 등 | `parkingFlag` (Y→true, N→false) |
| `indoor_outdoor_type` | `relaPosesFclty` 파싱 또는 acmpyTypeCd 추론 | `inOutFlag` |

---

### 5. place_accessibility — 무장애 접근성

```sql
CREATE TABLE place_accessibility (
    place_id            BIGINT      NOT NULL,
    has_parking         BOOLEAN     NOT NULL DEFAULT FALSE,
    stroller_accessible BOOLEAN     NOT NULL DEFAULT FALSE,
    has_ramp            BOOLEAN     NOT NULL DEFAULT FALSE,
    data_available      BOOLEAN     NOT NULL DEFAULT FALSE,  -- 무장애여행 API 승인 후 TRUE

    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;
```

**설계 결정:**
- `data_available = FALSE` → 접근성 점수 0점 fallback 처리 (펫워크지수.md 기준)
- `has_parking`은 `place_operations.parking_available`과 중복이지만, 무장애여행 API 기준 주차는 별도 의미 (장애인 주차공간 등)
- 무장애여행 API 승인 후 배치 업데이트

---

### 6. place_scores — 펫-워크 지수

```sql
CREATE TABLE place_scores (
    place_id                    BIGINT      NOT NULL,
    pet_companion_score         TINYINT     NOT NULL DEFAULT 0,   -- 0~30점
    workcation_score            TINYINT     NOT NULL DEFAULT 0,   -- 0~25점
    walk_accessibility_score    TINYINT     NOT NULL DEFAULT 5,   -- 0~20점 (fallback=5)
    congestion_score            TINYINT     NOT NULL DEFAULT 8,   -- 0~15점 (fallback=8)
    emergency_score             TINYINT     NOT NULL DEFAULT 0,   -- 0~7점
    accessibility_score         TINYINT     NOT NULL DEFAULT 0,   -- 0~3점 (fallback=0)
    total_score                 TINYINT     NOT NULL DEFAULT 0,   -- 0~100점
    grade                       CHAR(1),                          -- A|B|C|D|E
    calculated_at               DATETIME,

    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;
```

**등급 기준 (펫워크지수.md):**

| grade | total_score |
|---|---|
| A | 85점 이상 |
| B | 70~84점 |
| C | 55~69점 |
| D | 40~54점 |
| E | 39점 이하 |

**API 미승인 상태 fallback 기본값:**
- `walk_accessibility_score` = 5 (두루누비 미승인)
- `congestion_score` = 8 (관광지집중률 미승인 or UNKNOWN)
- `accessibility_score` = 0 (무장애여행 미승인)

---

### 7. congestion_forecasts — 관광지 혼잡도 예측

```sql
CREATE TABLE congestion_forecasts (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    tats_nm     VARCHAR(200)    NOT NULL,   -- 관광지명 (장소명 매핑 키)
    region      VARCHAR(20)     NOT NULL,   -- GANGNEUNG|CHUNCHEON|WONJU
    area_cd     VARCHAR(10)     NOT NULL,   -- 51 (강원특별자치도)
    signgu_cd   VARCHAR(10)     NOT NULL,   -- 51150|51110|51130
    base_ymd    DATE            NOT NULL,   -- 예측 기준일 (향후 30일)
    cnctr_rate  DECIMAL(5, 2),             -- 혼잡률 (%)

    PRIMARY KEY (id),
    UNIQUE KEY uq_forecast (tats_nm, base_ymd),
    INDEX idx_region_date (region, base_ymd)
) CHARACTER SET utf8mb4;
```

**설계 결정:**
- `places`와 FK 없음 → 이름 기반 매핑 (`tats_nm ↔ places.name` 텍스트 매칭)
- 매핑 실패 시 해당 장소 `congestion_score = 8` (UNKNOWN fallback)
- 매일 최신 30일 예측 데이터 upsert

**혼잡도 등급 변환 (앱 레이어):**

| cnctr_rate | 등급 | congestion_score |
|---|---|---|
| ~25% | RELAXED | 15점 |
| 25~50% | NORMAL | 11점 |
| 50~75% | CROWDED | 7점 |
| 75%~ | VERY_CROWDED | 3점 |
| 매핑 없음 | UNKNOWN | 8점 |

---

### 8. walk_courses — 두루누비 산책 코스

```sql
CREATE TABLE walk_courses (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    source_id       VARCHAR(50),            -- 두루누비 코스 ID
    region          VARCHAR(20)     NOT NULL,
    course_name     VARCHAR(200),
    start_latitude  DECIMAL(10, 7)  NOT NULL,
    start_longitude DECIMAL(10, 7)  NOT NULL,
    distance_km     DECIMAL(6, 2),
    last_synced_at  DATETIME,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_region (region),
    INDEX idx_coords (start_latitude, start_longitude)
) CHARACTER SET utf8mb4;
```

**설계 결정:**
- `places`와 FK 없음 → 점수 계산 시 Haversine으로 `places.latitude/longitude` ↔ `start_latitude/start_longitude` 거리 계산
- 두루누비 API 승인 전: 테이블 비어 있음 → `walk_accessibility_score = 5` fallback

---

## 데이터 흐름

```
[초기 적재]
KTO areaBasedList2 (강릉/춘천/원주)
  → places (INSERT)
  → place_images (INSERT)
  → detailPetTour2 호출
      → place_pet_conditions (INSERT)
  → detailIntro2 호출
      → place_operations (INSERT)
  → place_accessibility (INSERT, data_available=FALSE)
  → place_scores (INSERT, fallback 값으로)

GWTO listPart.do (PC01/PC05, 강릉/춘천/원주 필터)
  → detailSeqPart.do 개별 호출
      → places (INSERT)
      → place_images (INSERT)
      → place_pet_conditions (INSERT)
      → place_operations (INSERT)
      → place_accessibility (INSERT, data_available=FALSE)
      → place_scores (INSERT, fallback 값으로)

[배치: 일 1회]
관광지집중률 API
  → congestion_forecasts (UPSERT)
  → place_scores.congestion_score 재계산

[배치: API 승인 후]
두루누비 API
  → walk_courses (UPSERT)
  → place_scores.walk_accessibility_score 재계산

무장애여행 API
  → place_accessibility (UPDATE, data_available=TRUE)
  → place_scores.accessibility_score 재계산
```

---

## 미결 사항

| 항목 | 내용 |
|---|---|
| KTO `acmpyTypeCd` 실제 코드값 | ✅ 확인 완료 → 아래 매핑 확정 |
| KTO ↔ GWTO 중복 장소 처리 | 동일 장소가 두 소스에 존재 시 병합 전략 필요 (이름+주소 기준) |
| 두루누비 코스 응답 구조 | ✅ 확인 완료 → `walk_courses` 필드 확정 |
| `congestion_forecasts` 여행 날짜 적용 | 사용자 여행일 기준 `base_ymd` 필터 후 `cnctr_rate` 조회 |

### acmpyTypeCd → acmpy_type 매핑

| acmpyTypeCd (KTO 원문) | acmpy_type (DB 저장값) | 설명 |
|---|---|---|
| `전구역 동반가능` | `INDOOR_OUTDOOR` | 시설 전체 동반 가능 |
| `일부구역 동반가능` | `DESIGNATED` | 특정 구역(테라스 등)만 허용 |
| GWTO `inOutFlag` = `IN` | `INDOOR` | 실내만 |
| GWTO `inOutFlag` = `OUT` | `OUTDOOR` | 실외만 |
| GWTO `inOutFlag` = `INOUT` | `INDOOR_OUTDOOR` | 실내외 모두 |
| 값 없음 | `UNKNOWN` | 정보 없음 |

### 두루누비 강원 지역 현황 (2026-07-07 기준)

| 지역 | sigun 값 | 코스 수 |
|---|---|---|
| 강릉시 | `강원 강릉시` | 5개 |
| 춘천시 | - | 0개 (walk_accessibility_score fallback=5 적용) |
| 원주시 | - | 0개 (walk_accessibility_score fallback=5 적용) |
