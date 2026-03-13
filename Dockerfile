# Stage 1: Build
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY checkstyle.xml ./
COPY src/ src/
RUN ./mvnw package -DskipTests -B
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination target/extracted

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy AS runtime

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && addgroup --system spring && adduser --system --ingroup spring spring

WORKDIR /app

COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
