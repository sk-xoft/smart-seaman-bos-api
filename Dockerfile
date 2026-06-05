# Use official base image of Java Runtim
#FROM openjdk:11-slim-buster
FROM eclipse-temurin:11-jre

RUN apt-get update

RUN mkdir -p /apps-logs-service/smart-seaman-bos-api/logs
ENV TZ=Asia/Bangkok
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#CMD ["java", "-Xms512m", "-Xmx512m", "-jar", "/app.jar"]
#COPY target/*.jar /app.jar

# Set volume point to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside container
EXPOSE 8080

# Set application's JAR file
ARG JAR_FILE=target/*.jar

# Add the application's JAR file to the container
ADD ${JAR_FILE} app.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app.jar"]
# ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "/app.jar"]