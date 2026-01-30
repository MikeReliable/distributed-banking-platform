# ===== BUILD STAGE =====
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :user-service:bootJar --no-daemon

# ===== RUNTIME STAGE =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]