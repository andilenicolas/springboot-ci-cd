# Stage 1: Build the JAR file
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Run tests during the build stage
RUN mvn clean test

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file from the previous build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the properties file
COPY src/main/resources/application.properties /app/application.properties
COPY src/main/resources/application-docker.properties /app/application-docker.properties

# Copy test reports from build stage
COPY --from=build /app/target/surefire-reports/ /app/target/surefire-reports/

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]