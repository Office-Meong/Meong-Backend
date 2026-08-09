# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Gradle 의존성 캐시 (소스 변경 시 이 레이어는 재사용됨)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties ./
RUN ./gradlew dependencies --no-daemon -q

# 소스 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# pettravel.kr(GWTO)이 TLS 핸드셰이크 시 중간 인증서(Sectigo Public Server
# Authentication CA DV R36)를 보내지 않아 JVM 기본 트러스트스토어로는 인증서 체인
# 검증이 실패함(PKIX path building failed). 브라우저/curl은 OS 트러스트스토어나
# AIA로 보완하지만 JVM은 하지 않으므로, 해당 중간 인증서를 cacerts에 직접 등록함.
COPY docker/certs/sectigo-public-server-auth-ca-dv-r36.pem /tmp/gwto-intermediate.pem
RUN keytool -importcert -noprompt \
    -alias sectigo-public-server-auth-ca-dv-r36 \
    -file /tmp/gwto-intermediate.pem \
    -keystore "${JAVA_HOME}/lib/security/cacerts" \
    -storepass changeit \
    && rm /tmp/gwto-intermediate.pem

RUN addgroup --system app && adduser --system --ingroup app app
USER app

COPY --from=builder /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
