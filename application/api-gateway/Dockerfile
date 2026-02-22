# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . /workspace

# Build in-process "monolith mode" gateway and its required modules.
# Keep it testless for image build speed (tests are run in CI/local).
RUN mvn -q -DskipTests -pl application/api-gateway -am package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/application/api-gateway/target/api-gateway-*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
