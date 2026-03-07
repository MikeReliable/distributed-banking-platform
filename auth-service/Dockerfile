FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
