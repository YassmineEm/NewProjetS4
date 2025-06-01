stages:
  - dockerize
  - deploy

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: ""  # Important pour éviter les erreurs TLS avec docker:dind

before_script:
  - echo "Using Java ${javaVersion}"

dockerize:
  stage: dockerize
  image: docker:latest
  services:
    - docker:dind
  variables:
    IMAGE_TAG: "${dockerRepository!"your-default-repo"}/${artifactId}:latest"
  script:
    - docker info
    - docker build -t $IMAGE_TAG .
    - echo "$CI_REGISTRY_PASSWORD" | docker login -u "$CI_REGISTRY_USER" --password-stdin
    - docker push $IMAGE_TAG

deploy:
  stage: deploy
  script:
    - echo "Deploy step (à personnaliser selon ton environnement)"

