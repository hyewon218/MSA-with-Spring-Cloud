FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/catalogs-service-1.0.jar catalogs-service.jar
ENTRYPOINT ["java", "-jar", "catalogs-service.jar"]