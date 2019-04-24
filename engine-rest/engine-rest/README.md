REST API
========

A JAX-RS-based REST API for Camunda BPM.

Running Tests
-------------

The REST API is tested against three JAX-RS runtimes:

* Jersey
* Resteasy
* Wink

In order to run the tests against any of these, execute `mvn clean install -P${runtime}` where `${runtime}` is either `jersey`, `resteasy`, or `wink`. `jersey` is active by default.

Writing Tests
-------------

For a test case that tests the implementation of a JAX-RS resource, do the following:

* Subclass `org.camunda.bpm.engine.rest.AbstractRestServiceTest`
* Declare an instance of `org.camunda.bpm.engine.rest.util.container.TestContainerRule` as a JUnit `@ClassRule`
