# Multi-stage build for optimized image size
# Étape 1 : Build
FROM maven:3.9-eclipse-temurin-17 AS builder

# Métadonnées
LABEL maintainer="taskify-team@example.com"
LABEL version="1.0"
LABEL description="Application Taskify Backend - Gestion de tâches et rendez-vous"

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src ./src


RUN mvn clean package -DskipTests

# Étape 2 : Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
