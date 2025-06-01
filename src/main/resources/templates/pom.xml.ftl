<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>${description.springBootVersion!'3.4.4'}</version>
        <relativePath/>
    </parent>
    
    <groupId>${description.groupId!'com.example'}</groupId>
    <artifactId>${description.artifactId!'my-project'}</artifactId>
    <version>${description.version!'0.0.1-SNAPSHOT'}</version>
    <name>${description.name!description.artifactId}</name>
    
    <properties>
        <java.version>${(description.javaVersion!'17')?replace("^([0-9]+).*", "$1", "r")}</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <#if description.dependencies??>
          <#list description.dependencies as dependency>
            <#if dependency == "web">
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                </dependency>
            </#if>
            <#if dependency == "data-jpa">
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-jpa</artifactId>
                </dependency>
            </#if>
          </#list>
        </#if>

        <dependency>
          <groupId>io.springfox</groupId>
          <artifactId>springfox-swagger2</artifactId>
          <version>2.9.2</version>
        </dependency>
        <dependency>
           <groupId>io.springfox</groupId>
           <artifactId>springfox-swagger-ui</artifactId>
           <version>2.9.2</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>