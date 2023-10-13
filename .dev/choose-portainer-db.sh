#bin/bash

USER_DB_CHOICE=$1
CAMUNDA_RUN_HOME=$2

if [ -z "${USER_DB_CHOICE}" ]; then
  echo "The script requires a given db to find it's available portainer versions";
  exit 1;
fi

DB_VERSIONS=$(curl -s 'https://github.com/camunda/portainer-templates/tree/master/stacks' | jq '.payload.tree.items.[] |
.name' | grep -v "weblogic" | grep -v "websphere" | grep -v "tomcat" | grep -v "centos" | grep -v "ubuntu" | grep "$USER_DB_CHOICE" | grep "camunda-ci-")

printf "Select one of the available versions:\n"
echo "$DB_VERSIONS"
printf "\n"

read USER_SELECTED_DB_VERSION

PORTAINER_CURL_RESULT=$(curl -s "https://github.com/camunda/portainer-templates/blob/master/stacks/$USER_SELECTED_DB_VERSION/docker-stack.yml");

DOCKER_IMAGE=$(echo "$PORTAINER_CURL_RESULT" | yq '.payload.blob.rawLines | .[]' | yq '.services.main.image')
DOCKER_PORTS=$(echo "$PORTAINER_CURL_RESULT" | yq '.payload.blob.rawLines | .[]' | yq e -o=json '.services.main.ports')

# Remove new line characters
DOCKER_PORTS=$(echo "$DOCKER_PORTS" | tr -d '\n')

# Remove /tcp suffixes found in the ports
DOCKER_PORTS=$(echo "$DOCKER_PORTS" | sed -E 's/([0-9]+)/\1:\1/g')
DOCKER_PORTS=$(echo "$DOCKER_PORTS" | sed 's/\/tcp//g')

echo "$DOCKER_PORTS"

DOCKER_FILE=$(cat < docker-compose/db-template.yml | yq ".services.db.image=\"$DOCKER_IMAGE\"" | yq e ".services.db.ports = $DOCKER_PORTS")

echo "Printing docker file..."
#echo "$DOCKER_FILE"

echo "$DOCKER_FILE" | yq  > docker-compose/temp.yml

# Start docker-compose with the yml file created for the db of choice
docker-compose -f docker-compose/temp.yml up -d

# Start camunda-run to connect to the dockerized db
"$CAMUNDA_RUN_HOME"/start.sh