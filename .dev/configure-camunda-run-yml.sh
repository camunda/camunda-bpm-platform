#bin/bash

USER_DB_CHOICE="$1"
CAMUNDA_RUN_HOME="$2"

JDBC_URL=""
JDBC_DRIVER=""

case $USER_DB_CHOICE in
  "postgres")
   echo "configuring postgres"
   JDBC_URL="jdbc:postgresql://localhost:5432/process-engine"
   JDBC_DRIVER="org.postgresql.Driver"
   ;;
  "mysql")
   echo "configuring mysql";
   JDBC_URL="jdbc:mysql://localhost:3306/process-engine"
   JDBC_DRIVER="com.mysql.cj.jdbc.Driver"
   ;;
  "sqlserver")
   echo "configuring sqlserver"
   JDBC_URL="jdbc:sqlserver://localhost:1433;databaseName=process-engine;trustServerCertificate=true;integratedSecurity=true;"
   JDBC_DRIVER="com.microsoft.sqlserver.jdbc.SQLServerDriver"
   ;;
  "mariadb")
   echo "configuring mariadb"
   JDBC_URL="jdbc:mariadb://localhost:3306/process-engine"
   JDBC_DRIVER="org.mariadb.jdbc.Driver"
   ;;
  "oracle")
   echo "configuring oracle"
   JDBC_URL="jdbc:oracle:thin:@//localhost:1521/process-engine"
   JDBC_DRIVER="oracle.jdbc.OracleDriver"
   ;;
   *)
    echo "Unsupported Db. Exiting script."
    exit 1
esac

DB_USERNAME="camunda"
DB_PASSWORD="camunda"

# Replace YML DB Properties
yq -i ".\"spring.datasource\".url=\"$JDBC_URL\"" "$CAMUNDA_RUN_HOME"/configuration/default.yml
yq -i ".\"spring.datasource\".driver-class-name=\"$JDBC_DRIVER\"" "$CAMUNDA_RUN_HOME"/configuration/default.yml
yq -i ".\"spring.datasource\".username=\"$DB_USERNAME\"" "$CAMUNDA_RUN_HOME"/configuration/default.yml
yq -i ".\"spring.datasource\".password=\"$DB_PASSWORD\"" "$CAMUNDA_RUN_HOME"/configuration/default.yml