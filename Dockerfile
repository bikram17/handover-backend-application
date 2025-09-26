# Dockerfile
FROM eclipse-temurin:21-jdk
LABEL maintainer="backendtestinguser"
COPY target/api-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]