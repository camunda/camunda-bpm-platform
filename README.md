camunda BPM platform
====================

camunda BPM platform is a flexible framework for workflow and process automation. It's core is a native BPMN 2.0 process engine that runs inside the Java Virtual Machine. It can be embedded inside any Java application and any Runtime Container. It integrates with Java EE 6 and is a perfect match for the Spring Framework. On top of the process engine, you can choose from a stack of tools for human workflow management, operations & monitoring. 

* Web Site: http://www.camunda.org/
* Getting Started: http://www.camunda.org/app/implement-getting-started.html
* Contribution Guildelines: http://www.camunda.org/app/community-contribute.html


Building camunda BPM platform
----------
Apache Maven 3 and Java JDK 6 or 7 are prerequisites for building camunda BPM platform. Once you have setup Java and Maven, run

    mvn clean install

This will build all the modules that make up the camunda BPM platform but will not perform any integration testing. After the build is completed, you will find the distributions under

	distro/gf31/distro/target  (Apache Tomcat 7 Distribution)
    distro/tomcat/distro/target  (Glassfish 3 Distribution)
    distro/jbossas71/distro/target  (JBoss AS 7 Distribution)

Running Integration Tests
----------
The integration testsuites are located under `qa/`. There you'll find a folder named XX-runtime for each server runtime we support. These projects are responsible for taking a runtime container distribution (ie. Apache Tomcat, JBoss AS ...) and configuring it for integration testing. The actual integration tests are located in the `qa/integration-tests` module. This module contains an extensive testsuite that test the integration of the process engine within a particular runtime container. For example, such tests will ensure that if you use the Job Executor Service inside a Java EE Container, you get a proper CDI request context spanning multiple EJB invocations or that EE resource injection works as expected.

Integration tests are executed in-container, using [JBoss Arquillian](http://arquillian.org/).

In order to run the integration tests, first perform a full install build. Then navigate to the `qa` folder. 

For JBoss AS, run 

    mvn clean install -Pjboss

For JBoss AS ServletProcessApplication Support test, run

    mvn clean install -Pjboss,jboss-servlet

For Apache Tomcat, run

    mvn clean install -Ptomcat

For Glassfish, run

    mvn clean install -Pglassfish

