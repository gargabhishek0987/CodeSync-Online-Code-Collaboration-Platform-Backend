<<<<<<< HEAD:auth-service/Dockerfile
# Use Maven to build the application
=======
>>>>>>> origin/project-service:project-service/Dockerfile
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

<<<<<<< HEAD:auth-service/Dockerfile
# Use OpenJDK for the runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
=======
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
>>>>>>> origin/project-service:project-service/Dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
