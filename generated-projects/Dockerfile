FROM maven:3.9.9-jdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/projet.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
