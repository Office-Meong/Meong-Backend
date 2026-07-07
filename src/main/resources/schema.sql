-- OfficeMeong DB 스키마
-- charset: utf8mb4

CREATE TABLE IF NOT EXISTS places (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    source_type     VARCHAR(10)     NOT NULL,
    source_id       VARCHAR(50)     NOT NULL,
    place_type      VARCHAR(20)     NOT NULL,
    region          VARCHAR(20)     NOT NULL,
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

CREATE TABLE IF NOT EXISTS place_images (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    place_id        BIGINT          NOT NULL,
    image_url       VARCHAR(1000)   NOT NULL,
    is_thumbnail    BOOLEAN         NOT NULL DEFAULT FALSE,
    display_order   INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_place (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS place_pet_conditions (
    place_id            BIGINT          NOT NULL,
    acmpy_type          VARCHAR(20),
    is_cage_required    BOOLEAN         NOT NULL DEFAULT FALSE,
    is_leash_required   BOOLEAN         NOT NULL DEFAULT FALSE,
    pet_weight_limit_kg INT,
    cat_allowed         BOOLEAN         NOT NULL DEFAULT FALSE,
    bath_available      BOOLEAN         NOT NULL DEFAULT FALSE,
    companion_conditions TEXT,
    available_facilities TEXT,
    cautions            TEXT,
    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS place_operations (
    place_id            BIGINT          NOT NULL,
    operating_hours     VARCHAR(500),
    closed_days         VARCHAR(200),
    usage_fee           TEXT,
    parking_available   BOOLEAN,
    indoor_outdoor_type VARCHAR(10),
    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS place_accessibility (
    place_id            BIGINT      NOT NULL,
    has_parking         BOOLEAN     NOT NULL DEFAULT FALSE,
    stroller_accessible BOOLEAN     NOT NULL DEFAULT FALSE,
    has_ramp            BOOLEAN     NOT NULL DEFAULT FALSE,
    data_available      BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS place_scores (
    place_id                    BIGINT      NOT NULL,
    pet_companion_score         TINYINT     NOT NULL DEFAULT 0,
    workcation_score            TINYINT     NOT NULL DEFAULT 0,
    walk_accessibility_score    TINYINT     NOT NULL DEFAULT 5,
    congestion_score            TINYINT     NOT NULL DEFAULT 8,
    emergency_score             TINYINT     NOT NULL DEFAULT 0,
    accessibility_score         TINYINT     NOT NULL DEFAULT 0,
    total_score                 TINYINT     NOT NULL DEFAULT 0,
    grade                       CHAR(1),
    calculated_at               DATETIME,
    PRIMARY KEY (place_id),
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS congestion_forecasts (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    tats_nm     VARCHAR(200)    NOT NULL,
    region      VARCHAR(20)     NOT NULL,
    area_cd     VARCHAR(10)     NOT NULL,
    signgu_cd   VARCHAR(10)     NOT NULL,
    base_ymd    DATE            NOT NULL,
    cnctr_rate  DECIMAL(5, 2),
    PRIMARY KEY (id),
    UNIQUE KEY uq_forecast (tats_nm, base_ymd),
    INDEX idx_region_date (region, base_ymd)
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS walk_courses (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    source_id       VARCHAR(50),
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

-- Spring Batch 메타데이터 테이블은 spring.batch.jdbc.initialize-schema=always 설정으로 자동 생성
