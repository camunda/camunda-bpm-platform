#bin/bash

CAMUNDA_RUN_HOME="$1";

# If camunda run is not specified as a param, prompt the user to fill it in until it is
while [ -z "$CAMUNDA_RUN_HOME" ]; do
      # shellcheck disable=SC2162
      read -p "Please Specify camunda-run home: " CAMUNDA_RUN_HOME;
done

JDK_VERSION=$(java --version)

# verify java 17 is used before spinning up docker-compose
if [[ "$JDK_VERSION" != *"17"* ]]; then
  echo "Please set your JAVA HOME to a Java 17 to start camunda run";
  exit 1;
fi

USER_DB_CHOICE="$2";

while [[ "$USER_DB_CHOICE" != "postgres" && "$USER_DB_CHOICE" != "mysql" && "$USER_DB_CHOICE" != "sqlserver" &&
"$USER_DB_CHOICE" != "mariadb" && "$USER_DB_CHOICE" != "oracle" ]]; do
  # shellcheck disable=SC2162
  read -p "Please select the db of your choice: [postgres, mysql, sqlserver, mariadb, oracle]" USER_DB_CHOICE;
done

./configure-camunda-run-yml.sh "$USER_DB_CHOICE" "$CAMUNDA_RUN_HOME"

cp -r ./drivers/ "$CAMUNDA_RUN_HOME"/configuration/userlib

./choose-portainer-db.sh "$USER_DB_CHOICE" "$CAMUNDA_RUN_HOME"
