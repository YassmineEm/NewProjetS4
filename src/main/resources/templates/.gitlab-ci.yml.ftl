stages:
  - build
  - dockerize
  - deploy

variables:
  MAVEN_CLI_OPTS: "-s .m2/settings.xml --batch-mode --errors --fail-at-end"
  DOCKER_DRIVER: overlay2

before_script:
  - echo "Using Java ${javaVersion}"

build:
  stage: build
  image: maven:3.8.6-openjdk-${javaVersion}
  script:
    - mvn $MAVEN_CLI_OPTS clean package -DskipTests
  artifacts:
    paths:
      - target/${artifactId}.jar

dockerize:
  stage: dockerize
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker build -t ${dockerRepository!"your-default-repo"}/${artifactId}:latest .
    - docker push ${dockerRepository!"your-default-repo"}/${artifactId}:latest

deploy:
  stage: deploy
  script:
    - echo "Deploy step (à personnaliser selon ton environnement)"
