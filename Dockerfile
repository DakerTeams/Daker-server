FROM amazoncorretto:21-alpine
WORKDIR /app
COPY *.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
