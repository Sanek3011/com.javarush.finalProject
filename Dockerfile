FROM openjdk:17-jdk-slim

COPY target/jira-1.0.jar /app/application.jar

COPY resources /app/resources

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
