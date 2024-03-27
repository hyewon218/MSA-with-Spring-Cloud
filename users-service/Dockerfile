FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/users-service-1.0.jar users-service.jar
ENTRYPOINT ["java", "-jar", "users-service.jar"]
