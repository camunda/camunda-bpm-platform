# Examples

See example: https://github.com/camunda/camunda-external-task-client-java/tree/master/examples/loan-granting

1. start `loan-granting-webapp` (`mvn spring-boot:run`)
2. start client(s) (for `loan-granting-boot-client`: `mvn spring-boot:run`)

# Docker

1. Build with container : `mvn package -Pdocker`
2. start: `docker-compose up --scale client=2`
