This is a distribution of the 

   camunda fox bpm platform enterprise edition (ee)

camunda fox ee, (c) camunda services GmbH
 
Packaged jboss as 7 server is licensed under the LGPL license.

==================

Contents:

	client/
		This directory contains the client jar which is 
		needed for deploying process applications to the 
		fox platform.
	
	modules/
		This directory contains the jboss 7 modules which 
		make up the fox platform. You can use these modules 
		to patch a vanilla distribution of jboss as.
		
	server/
		This directory contains a preconfigured distribution 
		of jboss as 7 with camunda fox ce readily installed. 
		
		run the		
			server/jboss-as-${version.jboss.as}/bin/standalone.{bat/sh} 
		script to start up the the server.
		
		The server bundles a distribution of the activiti 
		task explorer (branded as fox-explorer).
		You can access it using the following URL:
		
		http://localhost:8080/explorer 
		
	examples/
		 This directory contains the sources and binaries for 
		 a set of examples showcasing the capabilities of the fox
		 platform. 

==================		
		
camunda fox ee version: ${project.version}
jboss as server version: ${version.jboss.as}

==================

Deploying the examples

In order to deploy the example projects located in the 'examples/' 
subdirectory, copy the examples from 'examples/bin' to the 
'server/jboss-as-${version.jboss.as}/standalone/deployments/ folder.
		 
After successful deployment, access the examples using 
the following URLs:		 
http://localhost:8080/example-cdi-jsf-taskmanagement 

==================

Building the examples

In order to build the examples from source you need apache maven.
1) Go to the 'examples/src/example-cdi-jsf-taskmanagement
   folder.
2) type mvn clean install
3) If the build is successfull, the deployable artifact (.WAR file)
   is located under 'target/' 
   
=================
   
Building the examples with integration tests

The examples also demonstrat how you can use jboss arquillian in 
order to perform in-container integration testing.

To this extend the test infrastructure assembles an application
server deployment (think of it as an in-memory .WAR file) and 
deploys it to an instance of jboss application server. 

You can choose between a 'managed' instance or a 'remote' instance
of Jboss AS. 

- in order to use the 'managed' container, type 
	mvn clean install -Pjboss-as-managed
  In that case arquillian will try to start the bundled jboss server,
  so make sure it is not already running.
  
- in order to use the 'remote' container, type 
	mvn clean install -Pjboss-as-remote
  In that case arquillian will try to connect to a running jboss server,
  in that case, make sure you start the server yourself.
  
INPORTANT NOTE: if you run the integration tests with arquillian, 
make sure the example application is not already deployed to the 
server! So if you already copied it to 
	server/jboss-as-${version.jboss.as}/standalone/deployments/ 
make sure to undeploy it first.

=================





