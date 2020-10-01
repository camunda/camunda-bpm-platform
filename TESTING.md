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
 * *integration-tests-webapps*: This module tests the camunda BPM webapplications inside the runtime containers. These integration tests run inside a client / server setting: the webapplication is deployed to the runtime container, the runtime container is started and the tests running inside a client VM perform requests against the deployed applications.

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
