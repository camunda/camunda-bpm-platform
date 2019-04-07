FROM openjdk:8-jdk-alpine
ARG JAR_FILE
COPY src/main/docker/entrypoint.sh /docker-entrypoint.sh
RUN chmod 777 /docker-entrypoint.sh
COPY ${JAR_FILE} app.jar
CMD ["sh", "./docker-entrypoint.sh"]
