This is a distribution of the 

   camunda fox bpm platform enterprise edition (ee)

camunda fox ee, (c) camunda services GmbH

==================

Contents:
		
	server/
		This directory contains a preconfigured distribution 
		of apache tomcat 6 with camunda fox cockpit readily installed. 
		
		run the		
			server/apache-tomcat-${tomcat6.version}/bin/startup.{bat/sh} 
		script to start up the the server.
		
		You can access camunda fox cockpit under:
		
		http://localhost:8080/cockpit 
		
    sql/
        This directory contains the create and upgrade sql scripts
        for the different databases.
        The engine create script contain the engine and history tables.
        
        Execute the current upgrade script to make the database compatible
        with the newest fox platform version.

==================		
		
camunda fox ee version: ${project.version}
tomcat server version: ${tomcat6.version}

=================
