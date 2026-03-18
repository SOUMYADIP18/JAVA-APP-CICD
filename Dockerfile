# Use a lightweight official Java runtime as the base image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your target folder into the container
COPY target/*.jar app.jar

# Expose port 8080 so the outside world can talk to the container
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]