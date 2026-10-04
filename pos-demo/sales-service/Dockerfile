# Build context is pos-demo/ so the build can read api/sales.yaml for code generation.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY api ./api
COPY sales-service/pom.xml sales-service/
COPY sales-service/src sales-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -f sales-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /src/sales-service/target/sales-service-0.1.0.jar app.jar
USER 1000
EXPOSE 8081
HEALTHCHECK --interval=5s --timeout=3s --start-period=30s --retries=10 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
