# -------------------------
# Stage 1: Build the JAR
# -------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set working directory inside container
WORKDIR /app

# Copy pom.xml and download dependencies first (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the project
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------
# Stage 2: Run the App
# -------------------------
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/habit-tracker.jar app.jar

# Expose the port from application.yml
EXPOSE 9090

# Do NOT hardcode DB credentials; use environment variables on Render
# ENV DB_URL=...
# ENV DB_USERNAME=...
# ENV DB_PASSWORD=...

# Run the jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
