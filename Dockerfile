FROM openjdk:17.0.1-jdk-slim
VOLUME /tmp
COPY ./app/build/libs/*.jar app.jar
COPY ./entrypoint.sh entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/bin/sh","entrypoint.sh"]