# Use a base image with both JDK and Maven (for building and running)
FROM maven:3.9.6-eclipse-temurin-17

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Package the application (includes downloading dependencies)
RUN mvn clean package -DskipTests

# Expose the application port (as in application.properties: 8089)
EXPOSE 8088

# Run the generated JAR (replace if your JAR has a different name)
CMD ["java", "-jar", "target/HireCraft-0.0.1-SNAPSHOT.jar"]