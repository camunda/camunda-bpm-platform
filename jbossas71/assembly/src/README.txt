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

==================		
		
camunda fox ee version: ${project.version}
jboss as server version: ${version.jboss.as}

=================
