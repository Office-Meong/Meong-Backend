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

RUN addgroup --system app && adduser --system --ingroup app app
USER app

COPY --from=builder /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
