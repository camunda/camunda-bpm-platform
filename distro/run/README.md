# Camunda REST Distribution

## Requirements

1. User profile:
 * Non-Java developer
 * Able to install the required JDK
 * Familiar with the command line and Rest APIs
2. No Application Server configuration, i.e., straightforward configuration
3. Pre-set security configuration

## Design Decisions

We decided to use the Camunda Spring Boot Starter. Reasons:
1. It produces an uberjar with all the dependencies provided.
2. It's straightforward to configure (`application.yml`) and to start.
3. Camunda already supports the technology, and it's easily extendable.

## How to run the Camunda Rest distro
1. Build the project with either the `ce` or `ee` profile
2. In `distro/target`, a `camunda-rest-distro-distro-{project.version}-[ce|ee].zip` will be available.
3. Extract the archive
4. Go to the `config` folder and add configuration to the application yml
5. Go to the `bin` folder and run `start.sh` (or `start.bat` if you're using Windows).
  * add the `--webapps` flag if you want to run the Camunda Webapps
  * add the `--rest` flag if you want to run the Camunda REST API
  
## Configuration

The distro can be configured through the `application.yml` file found in the `config/` directory
 of the distro archive. You can use the general camunda spring boot starter configuration properties available [here](https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/configuration/#camunda-engine-properties) as well as the following properties:
 
### Authentication

| Prefix           | Property Name   | Description                                                | Defaul Value |
|------------------|-----------------|------------------------------------------------------------|--------------|
| rest-distro.auth | .enabled        | Switch on/off authentication                               | true         |
|                  | .authentication | Authentication method.  Currently only basic is supported | basic        |
|                  |                 |                                                            |              |

### HTTPS/SSL

The following properties are provided by Spring Boot (see [Common Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#server-properties)). However, the properties listed below are necessary for Camunda BPM to run with HTTPS.

| Prefix     | Property Name       | Description                                                               | Defaul Value |
|------------|---------------------|---------------------------------------------------------------------------|--------------|
| server.ssl | .key-store          | Path to the key store that holds the SSL certificate (.jks or .p12 file). |              |
|            | .key-store-password | Password used to access the key store.                                    |              |
|            | .key-store-type     | Type of the key store (pkcs12 or jks).                                    |              |
|            | .key-alias          | Alias that identifies the key in the key store.                           |              |
|            | .key-password       | Password used to access the key in the key store.                         |              |
| server     | .port               | Server HTTPS port (e.g. 8443).                                            |              |

### CORS (Cross-Origin Resource Sharing)

| Prefix           | Property Name   | Description                                                            | Defaul Value |
|------------------|-----------------|------------------------------------------------------------------------|--------------|
| rest-distro.cors | .enabled        | Switch on/off CORS.                                                    | false        |
|                  | .allowedOrigins | Comma separated string with origins allowed origins or wildcard ("*"). | "*"          |

## Default values

By default, the admin credentials are set to `demo:demo`. This can be changed in the `config
/application.yml` configuration file.

### Database

The distro will use the H2 database by default. You will be able to find the database file in the
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
  url: jdbc:h2:./camunda-h2-dbs/process-engine;MVCC=TRUE;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE
  # the JDBC driver class
  driver-class-name: org.h2.Driver
  # the DB user
  username: sa
  # the DB password
  password: sa
```

## Learnings

* New authentication methods have to be implemented in the Engine Rest project through `Filters
`. Implementing custom authentication methods using `Spring Security` that make use of the
 Engines's authentication layer is not straightforward (however, this means that any new
  authentication methods are available anywhere the Engine Rest API is integrated).
* Spring (Boot) uses a custom `maven` property for adding external libraries to the classpath
 (`loader.path`). Using the default `java -cp` or `java -classpath` doesn't work.
* The `--spring.config.location` argument that passes the location of an external `application.yml
` configuration file should be passed after the `-jar` argument.
* The `spring-boot-maven-plugin` provides a `fatjar`. It also packages dependencies with a
 `provided` scope. We needed to explicitly exclude all of the `webapps` dependencies since we
  optionally include them if the user wants the Camunda Webapps loaded. 
* Enabling CORS is not straight-forward. The officially documented techniques to enable CORS as well as a filter-based
approach failed to attach the necessary response headers