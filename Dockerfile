# Use Maven to build the WAR
FROM maven:3.8.4-openjdk-8-slim AS build

WORKDIR /app

# Copy the entire project
COPY . /app/

# Build the WAR file
RUN mvn clean package -DskipTests

# Use OpenJDK 8 for the runtime
FROM openjdk:8-jdk-slim

WORKDIR /app

# Copy the built WAR file
COPY --from=build /app/target/pos-driver-0.0.1-SNAPSHOT.war /app/pos-driver.war

# Copy the Postilion ZIP file
COPY --from=build /app/libs/Realtime5.5.zip /app/libs/Realtime5.5.zip

# Extract the ZIP file inside the container
RUN apt-get update && apt-get install -y unzip && \
    unzip /app/libs/Realtime5.5.zip -d /app/libs/ && \
    rm /app/libs/Realtime5.5.zip && \
    apt-get remove -y unzip && apt-get clean

# Expose the application port
EXPOSE 8080

# Run Spring Boot with the correct classpath
ENTRYPOINT ["java", "-cp", "/app/pos-driver.war:/app/libs/*", "org.springframework.boot.loader.WarLauncher"]
