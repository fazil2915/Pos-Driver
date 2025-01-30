# Use an official Maven image to build the application
FROM maven:3.8.4-openjdk-8-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and the source code to the container
COPY pom.xml /app/
COPY src /app/src/

# Build the application using Maven
RUN mvn clean package -DskipTests

# Use an OpenJDK 8 image for the runtime environment
FROM openjdk:8-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the WAR file into the container
COPY target/pos-driver-0.0.1-SNAPSHOT.war /app/pos-driver-0.0.1-SNAPSHOT.war
# Expose the port the app runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/pos-driver-0.0.1-SNAPSHOT.war"]