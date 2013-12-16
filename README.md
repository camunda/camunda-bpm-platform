camunda BPM platform [![Build Status](https://buildhive.cloudbees.com/job/camunda/job/camunda-bpm-platform/badge/icon)](https://buildhive.cloudbees.com/job/camunda/job/camunda-bpm-platform/)
====================

The open source BPM platform

camunda BPM platform is a flexible framework for workflow and process automation. It's core is a native BPMN 2.0 process engine that runs inside the Java Virtual Machine. It can be embedded inside any Java application and any Runtime Container. It integrates with Java EE 6 and is a perfect match for the Spring Framework. On top of the process engine, you can choose from a stack of tools for human workflow management, operations & monitoring.

* Web Site: http://www.camunda.org/
* Getting Started: http://www.camunda.org/implement-getting-started.html
* Issue Tracker: https://app.camunda.com/jira
* Contribution Guildelines: http://www.camunda.org/community/contribute.html
* License: Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0

Components
----------

camunda BPM platform provides a rich set of components centered around the BPM lifecycle.

#### Process Implementation and Execution
 * camunda engine - The core component responsible for executing BPMN 2.0 processes.
 * REST API - The REST API provides remote access to running processes.
 * Spring, CDI integration - Programming model integration that allows developers to write Java Applications that interact with running processes.

#### Process Design
 * camunda modeler - A [modeler plugin for eclipse](https://github.com/camunda/camunda-modeler) that allows developers to design & refactor processes inside their IDE.
 * camunda cycle - Enables BPMN 2.0 based Roundtrip between Business and IT parties involved in a project. Allows to use any BPMN 2.0 modeling tool with camunda BPM.

#### Process Operations
 * camunda engine - JMX and advanced Runtime Container Integration for process engine monitoring.
 * camunda cockpit - Web application tool for process operations.

#### Human Task Management
 * camunda tasklist - Simple web application demonstrating how the process engine task API can be used.

#### And there's more...

 * [camunda-bpmn.js](https://github.com/camunda/camunda-bpmn.js) - We have started building a complete BPMN toolkit for Java Script (Parser, Process Engine, Renderer)
 * [camunda BPM incubation](https://github.com/camunda/camunda-bpm-incubation) - This is where we, together with the community, try out new ideas.


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

Apache Maven 3 and Java JDK 6 or 7 are prerequisites for building camunda BPM platform. Once you have setup Java and Maven, run

```
mvn clean install
```

This will build all the modules that make up the camunda BPM platform but will not perform any integration testing. After the build is completed, you will find the distributions under

```
distro/tomcat/distro/target     (Apache Tomcat 7 Distribution)
distro/gf31/distro/target       (Glassfish 3 Distribution)
distro/jbossas7/distro/target   (JBoss AS 7 Distribution)
```

Running Integration Tests
----------
The integration testsuites are located under `qa/`. There you'll find a folder named XX-runtime for each server runtime we support. These projects are responsible for taking a runtime container distribution (ie. Apache Tomcat, JBoss AS ...) and configuring it for integration testing. The actual integration tests are located in the `qa/integration-tests-engine` and `qa/integration-tests-webapps` modules.
 * *integration-tests-engine*: This module contains an extensive testsuite that test the integration of the process engine within a particular runtime container. For example, such tests will ensure that if you use the Job Executor Service inside a Java EE Container, you get a proper CDI request context spanning multiple EJB invocations or that EE resource injection works as expected. These integration tests are executed in-container, using [JBoss Arquillian](http://arquillian.org/).
 * *integration-tests-webapps*: This module tests the camunda BPM webapplications inside the runtime containers. These integration tests run inside a client / server setting: the webapplication is deployed to the runtime container, the runtime container is started and the tests running inside a client VM perform requests against the deployed applications.

In order to run the integration tests, first perform a full install build. Then navigate to the `qa` folder.

We have different maven profiles for selecting
* *Runtime containers & environments*: jboss, glassfish, tomcat
* *The testsuite*: engine-integration, webapps-integration
* *The database*: h2,h2-xa,db2,db2-xa,mssql,mssql-xa,oracle,oracle-xa,postgres,postgres-xa,mysql,mysql-xa (XA is only supprted on JBoss & Glassfish ATM)

In order to configure the build, compose the profiles for runtime container, testsuite, database. Example:

```
mvn clean install -Pengine-integration,jboss,h2
```

You can select multiple testsuites but only a single database and a single runtime container. This is valid:

```
mvn clean install -Pengine-integration,webapps-integration,tomcat,db2
```

There is a special profile for JBoss Application Server:

* Domain mode: `mvn clean install -Pengine-integration,h2,jboss-domain`

