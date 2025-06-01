# ${name!artifactId}

${description!""}

## Technologies
- Java ${javaVersion}
- Spring Boot ${springBootVersion}
- Build tool: ${buildTool}

## Architecture
${architectureType!"Standard"} architecture

## Getting Started

### Prerequisites
- JDK ${javaVersion}
<#if buildTool == "maven">
- Maven
<#else>
- Gradle
</#if>

### Installation
```sh
<#if buildTool == "maven">
./mvnw clean install
<#else>
./gradlew build
</#if>
