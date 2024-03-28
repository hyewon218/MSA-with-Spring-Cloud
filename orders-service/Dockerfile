FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/users-service-1.0.jar orders-service.jar
ENTRYPOINT ["java", "-jar", "orders-service.jar"]