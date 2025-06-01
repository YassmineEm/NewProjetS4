version: '3.8'
services:
  ${serviceName!"app"}:
    build: .
    ports:
      - "${port!"8080"}:${port!"8080"}"
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE=${profile!"prod"}