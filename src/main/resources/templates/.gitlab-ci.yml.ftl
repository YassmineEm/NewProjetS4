stages:
  - dockerize
  - deploy

variables:
  DOCKER_DRIVER: overlay2

before_script:
  - echo "Using Java ${javaVersion}"

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

