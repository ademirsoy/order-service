FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
COPY target/photo-shooting-order-service-1.0.jar /photo-shooting-order-service.jar
CMD ["java", "-jar", "/photo-shooting-order-service.jar"]
