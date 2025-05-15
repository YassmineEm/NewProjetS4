plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "org.emma"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("web")
    implementation("data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
