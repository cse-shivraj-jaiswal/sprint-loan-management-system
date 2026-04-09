# Run Stage (Optimized for 8GB RAM)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar discovery-server.jar

# 🚀 Hard memory limit for 8GB RAM systems
ENTRYPOINT ["java", "-Xmx256m", "-jar", "discovery-server.jar"]
