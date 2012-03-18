This is a distribution of the 

   camunda fox bpm platform community edition (ce)

camunda fox ce is licensed under the Apache License, Version 2.0. 
This does not include the packaged jboss as 7 server which is 
licensed under the LGPL license.

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
		
	examples/
		 This directory contains the sources and binaries for 
		 a set of examples showcasing the capabilities of the fox
		 platform. Note that when you start the pre-assembled server 
		 located under 'server/', the examples are already deloyed.
		 
		 Access the examples using the following URLs:
		 
		 http://localhost:8080/explorer
		 http://localhost:8080/example-cdi-jsf-taskmanagement 

==================		
		
camunda fox ce version: ${project.version}
jboss as server version: ${version.jboss.as}