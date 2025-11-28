FROM openjdk:8-jre-slim

WORKDIR /app

EXPOSE 8080

COPY target/onlineshop-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
