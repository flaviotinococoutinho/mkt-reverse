# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . /workspace

RUN mvn -q -DskipTests -pl modules/user-management -am package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/modules/user-management/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
