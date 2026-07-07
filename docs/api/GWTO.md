# 강원관광재단 반려동물 여행 API (GWTO / pettravel.kr) 분석

## 기본 정보

- 운영: 강원관광재단 (pettravel.kr)
- 인증: 불필요 (API 키 없이 호출 가능)
- 프로토콜: HTTPS (`https://www.pettravel.kr/api/`) — HTTP 요청은 301로 HTTPS 리다이렉트됨
- 데이터 포맷: JSON

> Android에서 `imageList` 이미지 URL은 `http://` 형식으로 제공됨 → `network_security_config.xml`에 `pettravel.kr` 도메인 허용 필요

---

## partCode (장소 유형 코드)

| partCode | 설명 | 서비스 PlaceType |
|---|---|---|
| PC01 | 카페 / 식당 | WORK_PLACE (keyword에 "카페" 포함) / FOOD |
| PC05 | 동물병원 | HOSPITAL |

---

## 엔드포인트

### `GET /detailSeqPart.do` — 장소 상세 조회 ✅ 정상 작동

```
GET https://www.pettravel.kr/api/detailSeqPart.do?partCode={partCode}&contentNum={contentSeq}
```

| 파라미터 | 설명 |
|---|---|
| partCode | 장소 유형 코드 (PC01, PC05 등) |
| contentNum | 장소 고유 번호 (목록의 순번이 아닌 `contentSeq` 값) |

### `GET /listPart.do` — 장소 목록 조회 ✅ 정상 작동

```
GET https://www.pettravel.kr/api/listPart.do?page={page}&pageBlock={pageBlock}&partCode={partCode}
```

| 파라미터 | 타입 | 설명 |
|---|---|---|
| partCode | String | 장소 유형 코드 (PC01, PC05 등) |
| page | int | 페이지 번호 |
| pageBlock | int | 페이지당 결과 수 (1~50, 범위 초과 시 10으로 처리) |

> **주의**: areaCode 파라미터 없음 — 전체 강원도 데이터를 반환하며, 응답의 `areaName` 필드로 지역 필터링 필요

**목록 응답 필드** (detail보다 간략)

| 필드 | 설명 |
|---|---|
| `contentSeq` | 장소 고유 번호 (detail 조회 시 사용) |
| `areaName` | 지역명 — 지역 필터링 기준 |
| `partName` | 분야명 |
| `title` | 장소명 |
| `address` | 주소 |
| `latitude` / `longitude` | 위도/경도 |
| `tel` | 전화번호 |
| `totalCount` | 전체 데이터 수 (페이징 계산용) |

> `keyword` 필드는 목록 응답에 없음 → 카페/식당 구분은 detail(`/detailSeqPart.do`) 호출 후 `keyword` 파싱 필요

**확인된 지역별 데이터 수 (2026-07-03 기준)**

| partCode | 강릉시 | 춘천시 | 원주시 | 전체(강원도) |
|---|---|---|---|---|
| PC01 (식음료) | 30개 | 46개 | 14개 | 154개 |
| PC05 (동물병원) | 12개 | 11개 | 102개 | 220개 |

#### ~~`/listSeqPart.do`~~ — 폐기된 엔드포인트

이전 버전 문서에 기재된 엔드포인트. 현재 호출 시 404 오류. 공식 매뉴얼(V2)에서 `/listPart.do`로 교체됨.

#### areaCode 값 (지역별 상세 조회용, `/detailSeqArea.do`에서 사용)

| 지역 | areaCode |
|---|---|
| 강릉시 | AC03 |
| 춘천시 | AC01 |
| 원주시 | AC02 |

---

## 응답 필드 (`/detailSeqPart.do` 기준, PC01 카페 확인)

### 기본 정보

| 필드 | 타입 | 설명 | 서비스 활용 |
|---|---|---|---|
| `contentSeq` | int | 장소 고유 번호 | 내부 식별자 |
| `title` | string | 장소명 | 장소명 표시 |
| `partName` | string | 장소 유형명 (예: 식음료) | - |
| `areaName` | string | 지역명 (예: 강릉시) | - |
| `address` | string | 주소 | 주소 표시 |
| `latitude` | string | 위도 (WGS84) | 지도 표시, 거리 계산 |
| `longitude` | string | 경도 (WGS84) | 지도 표시, 거리 계산 |
| `tel` | string | 전화번호 | 전화번호 표시 |
| `homePage` | string | 홈페이지 URL | 홈페이지 링크 |
| `content` | string | 장소 소개 | 개요 표시 |
| `keyword` | string | 쉼표 구분 키워드 | 카페/식당 분류 기준 |
| `imageList` | array | 이미지 URL 목록 (http://) | 이미지 표시 |

### 운영 정보

| 필드 | 타입 | 설명 | 서비스 활용 |
|---|---|---|---|
| `usedTime` | string | 운영시간 + 휴무일 통합 (예: `토,일 10:00-18:00. 평일 휴무`) | 운영시간·휴무일 표시 |
| `usedCost` | string | 이용 요금 및 메뉴 | 요금 안내 |
| `mainFacility` | string | 주요 시설 목록 | 이용 가능 시설 |
| `parkingFlag` | string | 주차 가능 여부 (`Y`/`N`) | 주차 정보 |

### 반려동물 조건

| 필드 | 타입 | 설명 | 서비스 활용 |
|---|---|---|---|
| `petFlag` | string | 반려동물 동반 가능 여부 (`Y`/`N`) | 동반 가능 여부 |
| `inOutFlag` | string | 이용 공간 (`IN`/`OUT`/`INOUT`) | 실내·실외 여부 |
| `petWeight` | string | 반려동물 체중 제한 (값 있는 경우) | 동반 조건 |
| `dogBreed` | string | 견종 구분 (`M` 등) | 동반 조건 |
| `petFacility` | string | 반려동물 전용 시설 | 이용 가능 시설 |
| `policyCautions` | string | 유의사항 (목줄, 켄넬, 안전 규정 등) | 유의사항 표시 |
| `bathFlag` | string | 목욕 시설 여부 (`Y`/`N`) | 부가 정보 |
| `entranceFlag` | string | 입장 가능 여부 (`Y`/`N`) | - |

### 기타

| 필드 | 타입 | 설명 |
|---|---|---|
| `provisionSupply` | string | 제공 용품 |
| `provisionFlag` | string | 제공 여부 (`Y`/`N`) |
| `restaurant` | string | 식당 관련 정보 |
| `parkingLog` | string | 주차 상세 안내 |
| `emergencyResponse` | string | 응급 대응 정보 |
| `emergencyFlag` | string | 응급 시설 여부 (`Y`/`N`) |
| `memo` | string | 메모 |

---

## 서비스 필드 매핑표

| 서비스 표시 정보 | GWTO 필드 | 비고 |
|---|---|---|
| 장소명 | `title` | |
| 주소 | `address` | |
| 위도/경도 | `latitude` / `longitude` | |
| 전화번호 | `tel` | |
| 운영시간 | `usedTime` | 휴무일 포함 |
| 휴무일 | `usedTime` (파싱) | 운영시간 필드에 통합 |
| 주차 가능 여부 | `parkingFlag` | Y/N |
| 반려견 동반 가능 여부 | `petFlag` | Y/N |
| 이용 가능 공간 | `inOutFlag` | IN/OUT/INOUT |
| 반려견 체중 제한 | `petWeight` | 빈값이면 제한 없음 |
| 유의사항 | `policyCautions` | |
| 이용 가능 시설 | `mainFacility`, `petFacility` | |
| 이미지 | `imageList[].image` | HTTP URL → Android 설정 필요 |
| 장소 소개 | `content` | |

---

## KTO 소스 장소와 비교

| 정보 항목 | KTO (KorPetTourService2) | GWTO |
|---|---|---|
| 반려견 동반 가능 여부 | `acmpyTypeCd` (상세 코드) | `petFlag` (Y/N 단순) |
| 동반 조건 / 유의사항 | `acmpyNeedMtr`, `etcAcmpyInfo` (별도 필드) | `policyCautions` (통합 텍스트) |
| 이용 가능 공간 | `relaPosesFclty`, `relaFrnshPrdlst` | `inOutFlag`, `mainFacility`, `petFacility` |
| 운영시간 | `detailIntro2` 별도 호출 필요 | `usedTime` 단일 필드 (휴무일 포함) |
| 주차 | `detailIntro2` 별도 호출 필요 | `parkingFlag` (Y/N 단일 필드) |
| 전화번호 | `areaBasedList2`에 포함 | 단일 응답에 포함 |
| 이미지 | `detailImage2` 별도 호출 또는 `firstimage` | `imageList` 단일 응답에 포함 |

> GWTO는 단일 `/detailSeqPart.do` 호출로 대부분의 정보를 제공. KTO는 여러 엔드포인트 호출 필요.

---

## 갭 분석 (GWTO가 제공하지 않는 항목)

| 정보 항목 | 상태 |
|---|---|
| 예측 혼잡도 | 없음 → 관광지집중률 API 별도 활용 |
| 접근성 (경사로, 유모차) | 없음 → 무장애여행 API 별도 활용 (미승인) |
| 반려견 동반 상세 코드 | `petFlag` Y/N만 제공, acmpyTypeCd 수준의 코드 없음 |
| 목록 조회 | ✅ `listPart.do` 정상 작동 확인 (page/pageBlock/partCode 파라미터) |
| 카페/식당 목록 구분 | 목록 응답에 `keyword` 없음 → detail 호출 후 `keyword` 파싱으로 구분 필요 |
