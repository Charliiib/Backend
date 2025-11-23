# Use Java 21 - imagen oficial de Eclipse Temurin
FROM eclipse-temurin:21-jdk-alpine

# Force HTTP/1.1 (Railway internal flag)
ENV RAILWAY_FORCE_HTTP1=true

# Disable HTTP/2 inside Spring just in case
ENV SERVER_HTTP2_ENABLED=false

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/comparar-0.0.1-SNAPSHOT.jar"]
