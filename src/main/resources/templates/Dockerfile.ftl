FROM maven:3.9.9-jdk-${javaVersion} AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM openjdk:${javaVersion}-jdk-slim
COPY --from=build /app/target/${artifactId}.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
