import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.testAnnotationProcessor
import org.gradle.kotlin.dsl.testCompileOnly

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    compileOnly ("org.projectlombok:lombok:1.18.32")
    annotationProcessor ("org.projectlombok:lombok:1.18.32")

    testCompileOnly ("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.32")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.5")
    }
}