camunda BPM - The open source BPM platform (WIP)  
==========================================

[![Build Status](https://travis-ci.org/camunda/camunda-bpm-platform.svg?branch=master)](https://travis-ci.org/camunda/camunda-bpm-platform)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-parent)

camunda BPM platform is a flexible framework for workflow and process automation. It's core is a native BPMN 2.0 process engine that runs inside the Java Virtual Machine. It can be embedded inside any Java application and any Runtime Container. It integrates with Java EE 6 and is a perfect match for the Spring Framework. On top of the process engine, you can choose from a stack of tools for human workflow management, operations & monitoring.

* Web Site: https://www.camunda.org/
* Getting Started: https://docs.camunda.org/get-started/
* User Forum: https://forum.camunda.org/
* Issue Tracker: https://app.camunda.com/jira
* Contribution Guidelines: https://camunda.org/contribute/
* License: The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).

Components
----------

camunda BPM platform provides a rich set of components centered around the BPM lifecycle.

#### Process Implementation and Execution
 * camunda engine - The core component responsible for executing BPMN 2.0 processes.
 * REST API - The REST API provides remote access to running processes.
 * Spring, CDI integration - Programming model integration that allows developers to write Java Applications that interact with running processes.

#### Process Design
 * camunda modeler - A [standalone desktop application](https://github.com/camunda/camunda-modeler) that allows business users and developers to design & configure processes.
 * camunda cycle - Enables BPMN 2.0 based Roundtrip between Business and IT parties involved in a project. Allows to use any BPMN 2.0 modeling tool with camunda BPM.

#### Process Operations
 * camunda engine - JMX and advanced Runtime Container Integration for process engine monitoring.
 * camunda cockpit - Web application tool for process operations.
 * camunda admin - Web application for managing users, groups, and their access permissions.

#### Human Task Management
 * camunda tasklist - Web application for managing and completing user tasks in the context of processes.

#### And there's more...

 * [bpmn.io](https://bpmn.io/) - Toolkits for BPMN, CMMN, and DMN in Java Script (rendering, modeling)
 * [Community Extensions](https://docs.camunda.org/manual/7.5/introduction/extensions/) - Extensions on top of Camunda BPM provided and maintained by our great open source community


A Framework
----------
In contrast to other vendor BPM platforms, camunda BPM strives to be highly integrable and embeddable. We seek to deliver a great experience to developers that want to use BPM technology in their projects.

### Highly Integrable
Out of the box, camunda BPM provides infrastructure-level integration with Java EE Application Servers and Servlet Containers.

### Embeddable
Most of the components that make up the platform can even be completely embedded inside an application. For instance, you can add the process engine and the REST Api as a library to your application and assemble your custom BPM platform configuration.


Building camunda BPM platform
----------

camunda BPM is available on maven central but for development of the platform, you have to add our public nexus repository to your maven settings.xml.
Add the following lines to it:

```xml
<profiles>
  <profile>
    <id>camunda-bpm</id>
    <repositories>
      <repository>
        <id>camunda-bpm-nexus</id>
        <name>camunda-bpm-nexus</name>
        <releases>
          <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
        <url>https://app.camunda.com/nexus/content/groups/public</url>
      </repository>
    </repositories>
  </profile>
</profiles>
<activeProfiles>
  <activeProfile>camunda-bpm</activeProfile>
</activeProfiles>
```

Apache Maven 3 and Java JDK 7/8 are prerequisites for building camunda BPM platform. Once you have setup Java and Maven, run

```
mvn clean install
```

This will build all the modules that make up the camunda BPM platform but will not perform any integration testing. After the build is completed, you will find the distributions under

```
distro/tomcat/distro/target     (Apache Tomcat 7 Distribution)
distro/jbossas7/distro/target   (JBoss AS 7 Distribution)
```

Running Integration Tests
----------
The integration testsuites are located under `qa/`. There you'll find a folder named XX-runtime for each server runtime we support. These projects are responsible for taking a runtime container distribution (ie. Apache Tomcat, JBoss AS ...) and configuring it for integration testing. The actual integration tests are located in the `qa/integration-tests-engine` and `qa/integration-tests-webapps` modules.
 * *integration-tests-engine*: This module contains an extensive testsuite that test the integration of the process engine within a particular runtime container. For example, such tests will ensure that if you use the Job Executor Service inside a Java EE Container, you get a proper CDI request context spanning multiple EJB invocations or that EE resource injection works as expected. These integration tests are executed in-container, using [JBoss Arquillian](http://arquillian.org/).
 * *integration-tests-webapps*: This module tests the camunda BPM webapplications inside the runtime containers. These integration tests run inside a client / server setting: the webapplication is deployed to the runtime container, the runtime container is started and the tests running inside a client VM perform requests against the deployed applications.

In order to run the integration tests, first perform a full install build. Then navigate to the `qa` folder.

We have different maven profiles for selecting
* *Runtime containers & environments*: jboss, tomcat, wildfly
* *The testsuite*: engine-integration, webapps-integration
* *The database*: h2,h2-xa,db2,sqlserver,oracle,postgresql,postgresql-xa,mysql (Only h2 / postgresql is supported in engine-integration tests)

In order to configure the build, compose the profiles for runtime container, testsuite, database. Example:

```
mvn clean install -Pengine-integration,jboss,h2
```

For using wildfly as the runtime container you have to additionally specify the wildfly version: wildfly8, wildfly10, wildfly11, wildfly12 or wildfly13. Example:

```
mvn clean install -Pengine-integration,wildfly,wildfly10,h2
```

If you want to test against an XA database, just add the corresponding XA database profile to the mvn cmdline above. Example:

```
mvn clean install -Pengine-integration,jboss,postgresql,postgresql-xa
```

You can select multiple testsuites but only a single database and a single runtime container. This is valid:

```
mvn clean install -Pengine-integration,webapps-integration,tomcat,postgresql
```

There is a special profile for JBoss Application Server:

* Domain mode: `mvn clean install -Pengine-integration,h2,jboss-domain`

Limiting the number of engine unit tests
----------
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
