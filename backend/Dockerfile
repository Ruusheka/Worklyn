
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src

RUN chmod +x mvnw \
    && ./mvnw -f backend/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
