# ===== BUILD STAGE =====
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :auth-service:bootJar --no-daemon

# ===== RUNTIME STAGE =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]