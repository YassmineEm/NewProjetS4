plugins {
    id 'java'
    id 'org.springframework.boot' version '${springBootVersion}'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = '${groupId}'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '${javaVersion}'

repositories {
    mavenCentral()
}

dependencies {
<#list dependencies?keys as key>
    implementation '${key}'
</#list>
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
