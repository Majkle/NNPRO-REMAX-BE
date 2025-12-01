# ===============================
# Stage 1: Build the Application
# ===============================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (offline mode)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# ===============================
# Stage 2: Run the Application
# ===============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR file
COPY --from=builder /app/target/*.jar app.jar

# This allows the app to write logs or create H2 database files in /app
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]