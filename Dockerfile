# Stage 1: Build the WAR using Maven
FROM maven:3.8.4-openjdk-8-slim AS build

WORKDIR /app

# Copy the entire project
COPY . /app/

# Copy the Postilion ZIP file before building
COPY /libs/Realtime5.5.zip /app/libs/Realtime5.5.zip

# Install unzip, extract Postilion SDK, then clean up
RUN apt-get update && apt-get install -y unzip && \
    unzip /app/libs/Realtime5.5.zip -d /app/libs/ && \
    rm /app/libs/Realtime5.5.zip && \
    apt-get remove -y unzip && apt-get clean

# Build the WAR file (ensuring Postilion SDK is available)
RUN mvn clean package -DskipTests

# Stage 2: Runtime Image with OpenJDK 8
FROM openjdk:8-jdk-slim

WORKDIR /app

# Copy the built WAR file
COPY --from=build /app/target/pos-driver-0.0.1-SNAPSHOT.war /app/pos-driver.war

# Copy the extracted Postilion SDK from the build stage
COPY --from=build /app/libs/ /app/libs/

# Expose the application port
EXPOSE 8080

# Run Spring Boot with the correct classpath
ENTRYPOINT ["java", "-cp", "/app/pos-driver.war:/app/libs/*", "org.springframework.boot.loader.WarLauncher"]
