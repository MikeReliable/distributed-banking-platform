pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.springframework.boot") version "3.1.6"
        id("io.spring.dependency-management") version "1.1.4"
    }
}

rootProject.name = "bank-rest"

include(
    "auth-service",
    "user-service",
    "card-service",
    "transfer-service",
    "api-gateway"
)
