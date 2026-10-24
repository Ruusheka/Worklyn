FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /workspace

COPY backend/pom.xml pom.xml
COPY backend/src ./src

RUN mvn -q clean package -DskipTests

FROM amazoncorretto:17
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
