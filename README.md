camunda BPM platform
====================

The open source BPM platform

camunda BPM platform is a flexible framework for workflow and process automation. It's core is a native BPMN 2.0 process engine that runs inside the Java Virtual Machine. It can be embedded inside any Java application and any Runtime Container. It integrates with Java EE 6 and is a perfect match for the Spring Framework. On top of the process engine, you can choose from a stack of tools for human workflow management, operations & monitoring.

* Web Site: http://www.camunda.org/
* Getting Started: http://www.camunda.org/app/implement-getting-started.html
* Contribution Guildelines: http://www.camunda.org/app/community-contribute.html
* License: Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0

Components
----------

camunda BPM platform provides a rich set of components centered around the BPM lifecycle. 

#### Process Implementation and Execution
 * Process Engine - The core component responsible for executing BPMN 2.0 processes.
 * REST API - The REST API provides remote access to running processes.
 * Spring, CDI - Programming model integration that allows developers to write Java Applications that interact with running processes.

#### Process Design
 * camunda modeler - A [modeler plugin for eclipse](https://github.com/camunda/camunda-modeler) that allows developers to design & refactor processes inside their IDE.
 * camunda cycle - Enables BPMN 2.0 based Roundtrip between Business and IT parties involved in a project. Allows to use any BPMN 2.0 modeling tool with camunda BPM.

#### Process Operations
 * process engine - JMX and advanced Runtime Container Integration for process engine monitoring.
 * camunda cockpit - Web application tool for process operations.

#### Human Task Management
 * camunda tasklist - Simple web application demonstrating how the process engine task API can be used.
 
#### And there's more...

 * [camunda-bpmn.js](https://github.com/camunda/camunda-bpmn.js) - We have started building a complete BPMN toolkit for Java Script (Parser, Process Engine, Renderer)
 * [camunda BPM sandbox](https://github.com/camunda/camunda-bpm-sandbox) - This is where we, together with the community, try out new ideas.


A Framework
----------
In contrast to other vendor BPM platforms, camunda BPM strives to be highly integrable and embeddable. We seek to deliver a great experience to developers that want to use BPM technology in their projects.

### Highly Integrable
Out of the box, camunda BPM provides infrastructure-level integration with Java EE Application Servers and Servlet Containers.

### Embeddable
Most of the components that make up the platform can even be completely embedded inside an application. For instance, you can add the process engine and the REST Api as a library to your application and assemble your custom BPM platform configuration.


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

