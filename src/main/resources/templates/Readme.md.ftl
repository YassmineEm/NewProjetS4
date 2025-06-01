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
- ${buildTool == "maven" ? "Maven" : "Gradle"}

### Installation
```sh
${buildTool == "maven" ? "./mvnw clean install" : "./gradlew build"}