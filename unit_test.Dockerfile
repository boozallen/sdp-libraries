FROM gradle:4.10.2-jdk8

USER root
COPY build.gradle /app/build.gradle
WORKDIR /app
