This is a distribution of the 

   camunda fox bpm platform for IBM Websphere Application Server (Enterprise Edition)

Version: ${project.version}

==================

Contents:

	client/
		This directory contains the client library which can be 
		bundled by process applications to connect to the 
		fox platform. 
	
	modules/
		Contains the fox platform modules:
			- fox-platform-ibm-websphere-ear-${project.version}.ear
				This is the fox platform application providing the process engine service.
			- fox-platform-jobexecutor-rar-${project.version}.rar
				This is the resource adapter (JCA Connector) for plugging the fox platform
				job executor into your application server.
	modules/lib
		Contains libraries that must be made available to both the fox platform and any 
		process applications.
  
  modules/java/jre/lib/ext
    Contains the encryption provider libraries that must be made available
    to the jdk/jre in websphere for license checking.
    Copy them into WAS_HOME/java/jre/lib/ext, there should be already some IBM libs present.
				
  webapps/
    Contains the fox platform webapps to deploy.
        
  sql/
    This directory contains the create and upgrade sql script for the different databases.
    The engine create script contain the engine and history tables.
    
    Execute the current upgrade script to make the database compatible
    with the newest fox platform version.

==================		

