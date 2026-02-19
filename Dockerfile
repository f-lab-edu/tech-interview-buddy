FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 서브프로젝트 설정 파일 복사
COPY app/build.gradle app/
COPY common/build.gradle common/
COPY recommend/build.gradle recommend/

# 소스 코드 복사
COPY app/src app/src
COPY common/src common/src
COPY recommend/src recommend/src

# 빌드 실행
RUN chmod +x ./gradlew
RUN ./gradlew :app:bootJar --no-daemon
RUN find /workspace/app/build/libs -name "*.jar" -type f

# 실행 단계
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 JAR 파일 복사 (멀티 모듈에서는 app 모듈의 bootJar)
# 와일드카드가 작동하지 않을 수 있으므로 명시적으로 파일명 지정
COPY --from=build /workspace/app/build/libs/tech-interview-buddy-app-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
