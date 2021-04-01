Running Integration Tests
----------
The integration testsuites are located under `qa/`. There you'll find a folder named XX-runtime for each server runtime we support. These projects are responsible for taking a runtime container distribution (ie. Apache Tomcat, Wildfly) and configuring it for integration testing. The actual integration tests are located in the `qa/integration-tests-engine` and `qa/integration-tests-webapps` modules.
 * *integration-tests-engine*: This module contains an extensive testsuite that test the integration of the process engine within a particular runtime container. For example, such tests will ensure that if you use the Job Executor Service inside a Java EE Container, you get a proper CDI request context spanning multiple EJB invocations or that EE resource injection works as expected. These integration tests are executed in-container, using [JBoss Arquillian](http://arquillian.org/).
 * *integration-tests-webapps*: This module tests the Camunda Platform webapplications inside the runtime containers. These integration tests run inside a client / server setting: the webapplication is deployed to the runtime container, the runtime container is started and the tests running inside a client VM perform requests against the deployed applications.

In order to run the integration tests, first perform a full install build. Then navigate to the `qa` folder.

We have different maven profiles for selecting
* *Runtime containers & environments*: tomcat, wildfly
* *The testsuite*: engine-integration, webapps-integration
* *The database*: h2,h2-xa,postgresql,postgresql-xa (XA is only supported on Wildfly atm)

In order to configure the build, compose the profiles for runtime container, testsuite, database. Example:

```
mvn clean install -Pengine-integration,tomcat,h2
```

Here's another example for using wildfly as the runtime container:

```
mvn clean install -Pengine-integration,wildfly,h2
```

If you want to test against an XA database, just add the corresponding XA database profile to the mvn cmdline above. Example:

```
mvn clean install -Pengine-integration,wildfly,postgresql,postgresql-xa
```

You can select multiple testsuites but only a single database and a single runtime container. This is valid:

```
mvn clean install -Pengine-integration,webapps-integration,tomcat,postgresql
```

There is a special profile for Wildfly Application Server:

* Domain mode: `mvn clean install -Pengine-integration,h2,wildfly-domain`

### Running tests with the Maven Wrapper

With `mvnw`, from the root of the project,
run: `./mvnw clean install -f qa/pom.xml -P${integration-test-id},${application-server-id},${database-id}`
where `${database-id}` is for example `h2`, `${application-server-id}` is for e.g. `wildfly,wildfly-domain`,
and `${integration-test-id}` can be either `engine-integration` or `webapps-integration`.