# Testing Guidelines

* [Best Practices for Writing Test Cases](#best-practices-for-writing-test-cases)
* [Running Integration Tests](#running-integration-tests)
* [Limiting the Number of Engine Unit Tests](#limiting-the-number-of-engine-unit-tests)

# Best Practices for Writing Test Cases

* write JUnit4-style tests, not JUnit3
* Project `camunda-engine`: If you need a process engine object, use the JUnit rule `org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule`. It ensures that the process engine object is reused across test cases and that certain integrity checks are performed after every test. For example:
  ```
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  public void testThings() {
    ProcessEngine engine = engineRule.getProcessEngine();

    ...
  }
  ```
* Project `camunda-engine`: As an alternative to the above, you can extend extend the `org.camunda.bpm.engine.test.util.PluggableProcessEngineTest` class.
  The class already provides an instance of the `ProvidedProcessEngineRule`, as well as the `ProcessEngineTestRule` that
  provides some additional custom assertions and helper methods.
  * However, if you need to make modifications to the `ProcessEngineConfiguration`, then please use the `ProcessEngineBootstrapRule`
    as described below. 
* Project `camunda-engine`: If you need a process engine with custom configuration, use the JUnit rule `org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule` and chain it with `org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule` like so:
  ```
  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      // apply configuration options here
  });
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);
  ```
  
# Running Integration Tests

The integration test suites are located under `qa/`. There you'll find a folder named XX-runtime for 
each server runtime we support. These projects are responsible for taking a runtime container 
distribution (ie. Apache Tomcat, WildFly AS ...) and configuring it for integration testing. The 
actual integration tests are located in the `qa/integration-tests-engine` and `qa/integration-tests-webapps` modules.
 * *integration-tests-engine*: This module contains an extensive testsuite that test the integration of the process engine within a particular runtime container. For example, such tests will ensure that if you use the Job Executor Service inside a Java EE Container, you get a proper CDI request context spanning multiple EJB invocations or that EE resource injection works as expected. These integration tests are executed in-container, using [JBoss Arquillian](http://arquillian.org/).
 * *integration-tests-webapps*: This module tests the Camunda Platform webapplications inside the runtime containers. These integration tests run inside a client / server setting: the webapplication is deployed to the runtime container, the runtime container is started and the tests running inside a client VM perform requests against the deployed applications.

In order to run the integration tests, first perform a full install build. Then navigate to the `qa` folder.

We have different maven profiles for selecting
* *Runtime containers & environments*: tomcat, wildfly
* *The testsuite*: engine-integration, webapps-integration
* *The database*: h2,h2-xa,db2,sqlserver,oracle,postgresql,postgresql-xa,mysql (Only h2 and 
  postgresql are supported in engine-integration tests)

In order to configure the build, compose the profiles for runtime container, testsuite, database. Example:

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

There is a special profile for the WildFly Application Servers:

* WildFly Domain mode: `mvn clean install -Pengine-integration,h2,wildfly-domain`

# Testing a Given Database

Camunda supports all database technologies listed on [Supported Database Products](https://docs.camunda.org/manual/latest/introduction/supported-environments/#supported-database-products), and in all environments, they are operating in as specified. Support means we guarantee the Camunda Platform integrates well with the database technology’s JDBC behavior (there are some [documented](https://docs.camunda.org/manual/latest/user-guide/process-engine/database/) limitations, e.g., isolation level `READ_COMMITTED` is required for all databases). We test a database technology with a specific database, i.e., we test it in one environment, not all possible environments that you can imagine (e.g., we test Postgres on local Docker containers, but not as hosted databases on AWS or Azure).

# No Maven? No problem!

This project provides a [Maven Wrapper](https://github.com/takari/maven-wrapper). This feature is useful for developers
to build and test the project with the same version that Camunda uses. It's also useful for developers that don't want
to install Maven at all. By executing the `mvnw` script (Unix), or `mvnw.cmd` script (Windows), a Maven distro will be 
downloaded and installed in the `$USER_HOME/.m2/wrapper/dists` folder of the system. You can check the download URL in
the [.mvn/wrapper/maven-wrapper.properties](.mvn/wrapper/maven-wrapper.properties) file.

The Maven Wrapper requires Maven commands to be executed from the root of the project. As the Camunda Platform project
is a multi-module (Maven Reactor) project, this is also a good best practice to apply.

To build the whole project, or just a module, one of the following commands may be executed:

```shell
# build the whole project
./mvnw clean install

# build the engine module
./mvnw clean install -f engine/pom.xml

# run the rolling-update IT tests with the H2 database
./mvnw verify -f qa/test-db-rolling-update/pom.xml -Prolling-update,h2
```

> Note: Above the `mvn -f` command line option is recommended over the `mvn -pl` option. The reason is that `-pl` will
build only the specified module, and will ignore any sub-modules that it might contain (unless the `-amd` option is also
added). As the Camunda Platform project has a multi-tiered module hierarchy (e.g. the [qa](qa/) module has modules of 
it's own), the `mvn -f` command option is simpler. 

## What about database technology X in environment Y?

To make a statement regarding Camunda Platform support, we need to understand if technology X is one of the technologies we already support or different technology. Several databases may share the same or a similar name, but they can still be different technologies: For example, IBM DB2 z/OS behaves quite differently from IBM DB2 on Linux, Unix, Windows. Amazon Aurora Postgres is different from a standard Postgres.

If you want to make sure that a given database works well with the Camunda Platform, you can run the test suite against this database.

In the `pom.xml` file located in the `./database` folder, several database profiles are defined with a matching database driver.

To run the test suite against a given database, select the `database` profile and your desired database profile and provide the connection parameters:

```
mvn test -Pdatabase,postgresql -Ddatabase.url=jdbc:postgresql:pgdb -Ddatabase.username=pguser -Ddatabase.password=pgpassword
```

## Testing a Camunda-supported Database with Testcontainers

It is also possible to use Testcontainers to run the test suite agains a given database. To ensure that your database 
Docker image can be used this way, please perform the following steps:

1. Ensure that your Docker image is compatible with Testcontainers;
1. Provide the repository name of your Docker image in the [testcontainers.properties](./engine/src/test/resources/testcontainers.properties) file;
   * If you use a private Docker repository, please include it in the Docker image name (e.g. private.registry.org/postgres)
1. In the `pom.xml` file located in the `./database` folder, check out the `database.tc.url` property to ensure that 
   the Docker tags match.
1. Make sure that the `testcontainers` profile is added to your Maven `settings.xml` (you can find it [here](settings/maven/nexus-settings.xml)).

At the moment, Testcontainers can be used with the Camunda-supported versions of the following databases. Please make 
sure that the database image is configured according to [this guide](https://docs.camunda.org/manual/latest/user-guide/process-engine/database/database-configuration/#isolation-level-configuration):
* PostgreSQL
* MariaDB
* MySQL
* MS-SQL 2017/2019 ([MSSQL-specific configuraion guide](https://docs.camunda.org/manual/latest/user-guide/process-engine/database/mssql-configuration/))

To execute the process engine test suite with a certain database (e.g. PostgreSQL), you should call Maven in the 
engine directory with
```shell
mvn clean test -Ppostgresql,testcontainers
```

# Limiting the Number of Engine Unit Tests

Due to the fact that the number of unit tests in the camunda engine increases daily and that you might just want to test a certain subset of tests the maven-surefire-plugin is configured in a way that you can include/exclude certain packages in your tests.

There are two properties that can be used for that: ``test.includes`` and ``test.excludes``

When using the includes only the packages listed will be include and with excludes the other way around.
For example calling Maven in the engine directory with
```
mvn clean test -Dtest.includes=bpmn
```
will test all packages that contain "bpmn". This will include e.g. ``*test.bpmn*`` and ``*api.bpmn*``. If you want to limit this further you have to get more concrete. Additionally, you can combine certain packages with a pipe:
```
mvn clean test -Dtest.includes=bpmn|cmmn
```
will execute all bpmn and cmmn tests.

The same works for excludes. Also, you can combine both:
```
mvn clean test -Dtest.includes=bpmn -Dtest.excludes=bpmn.async
```
Please note that excludes take precedence over includes.

To make it easier for you we created some profiles with predefined in- and excludes:
- testBpmn
- testCmmn
- testBpmnCmmn
- testExceptBpmn
- testExceptCmmn
- testExceptBpmnCmmn

So simply call
```
mvn clean test -PtestExceptBpmn
```
and all the bpmn testcases won't bother you any longer.
