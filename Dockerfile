# Use the official Kotlin runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the fat JAR into the image
COPY ./build/libs/com.example.datacollectorserver-all.jar /app/com.example.datacollectorserver-all.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "/app/your-application.jar"]
