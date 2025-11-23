# Use Java 21
FROM openjdk:21-slim

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Install Maven and build
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/comparar-0.0.1-SNAPSHOT.jar"]