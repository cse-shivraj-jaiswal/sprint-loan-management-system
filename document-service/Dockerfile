# Run Stage (Optimized for 8GB RAM)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar document-service.jar

# 🚀 Hard memory limit for 8GB RAM systems
ENTRYPOINT ["java", "-Xmx256m", "-jar", "document-service.jar"]
