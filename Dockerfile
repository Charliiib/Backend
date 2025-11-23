# Use Java 21 - imagen oficial de Eclipse Temurin
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Make mvnw executable and build the application
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/comparar-0.0.1-SNAPSHOT.jar"]