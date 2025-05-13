plugins {
    java
    id("org.springframework.boot") version "${springBootVersion}"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "${groupId}"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_${javaVersion}

repositories {
    mavenCentral()
}

dependencies {
<#list dependencies?keys as key>
    implementation("${key}")
</#list>
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
