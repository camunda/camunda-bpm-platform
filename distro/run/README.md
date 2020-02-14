# Camunda BPM Run

This is a pre-configured Camunda Spring Boot distribution that allows users to simply download and RUN Camunda BPM.

## How to run it
1. Build the project with either the `ce` or `ee` profile
2. In `distro/target`, a `camunda-bpm-run-distro-{project.version}-[ce|ee].zip` will be available.
3. Extract the archive
4. Go to the `config` folder and add configuration to the `application.yml`
5. Go to the `bin` folder and run `start.sh` (or `start.bat` if you're using Windows).
   * add the `--webapps` flag if you want to run the Camunda Webapps only
   * add the `--rest` flag if you want to run the Camunda REST API only
   * add the `--webapps` and the `--rest` flags if you want to run both (default)
  
## Configuration

Camunda BPM Run can be configured through the `application.yml` file found in the `config/` directory. 
You can use the general Camunda Spring Boot Starter configuration properties available [here](https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/configuration/#camunda-engine-properties) as well as the following additional properties:
 
### Authentication

| Prefix               | Property Name   | Description                                                | Default Value |
|----------------------|-----------------|------------------------------------------------------------|---------------|
| camunda.bpm.run.auth | .enabled        | Switch on/off authentication                               | true          |
|                      | .authentication | Authentication method.  Currently only basic is supported  | basic         |

### HTTPS/SSL

The following properties are provided by Spring Boot (see [Common Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#server-properties)). However, the properties listed below are necessary for Camunda BPM to run with HTTPS.

| Prefix     | Property Name       | Description                                                               | Default Value |
|------------|---------------------|---------------------------------------------------------------------------|---------------|
| server.ssl | .key-store          | Path to the key store that holds the SSL certificate (.jks or .p12 file). |               |
|            | .key-store-password | Password used to access the key store.                                    |               |
|            | .key-store-type     | Type of the key store (pkcs12 or jks).                                    |               |
|            | .key-alias          | Alias that identifies the key in the key store.                           |               |
|            | .key-password       | Password used to access the key in the key store.                         |               |
| server     | .port               | Server HTTPS port (e.g. 8443).                                            |               |

### CORS (Cross-Origin Resource Sharing)

| Prefix               | Property Name   | Description                                                            | Default Value |
|----------------------|-----------------|------------------------------------------------------------------------|---------------|
| camunda.bpm.run.cors | .enabled        | Switch on/off CORS.                                                    | false         |
|                      | .allowedOrigins | Comma separated string with origins allowed origins or wildcard ("*"). | "*"           |

## Default values

By default, the admin credentials are set to `demo:demo`. This can be changed in the `config/application.yml` configuration file.

### Database

Camunda BPM Run will use the H2 database by default. You will be able to find the database file in the
 `bin/camunda-h2-dbs` directory after the initial run. To connect to a different database (out of
  the ones [supported by Camunda](https://docs.camunda.org/manual/latest/introduction/supported-environments/#databases))
 , you will need to perform the following steps:
1. Put the appropriate JDBC driver `.jar` archive into the `lib/db` directory.
   * Optionally remove the `h2` JDBC driver that is already there. It will not be needed anymore.
2. Modify the following code of the `config/application.yml` configuration file with the correct DB
 connection info:
```yaml
spring.datasource:
  # the DB url and DB Schema name (common template: jdbc:{db-type}://{ip-address}:{port}/{db-schema-name}
  url: jdbc:h2:./camunda-h2-dbs/process-engine;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE
  # the JDBC driver class
  driver-class-name: org.h2.Driver
  # the DB user
  username: sa
  # the DB password
  password: sa
```
