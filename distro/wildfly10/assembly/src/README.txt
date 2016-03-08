This is a distribution of 

       camunda BPM platform v${project.version}
          
visit     
       http://docs.camunda.org/

   
camunda BPM platform is licensed under the Apache License v2.0
http://www.apache.org/licenses/LICENSE-2.0
 
The packaged Wildfly Application Server is licensed under
the LGPL license.

==================

Contents:

  lib/
        This directory contains the java libraries for application 
        development.
  
  modules/
        This directory contains additional modules for Wildfly Application
        Server. You can use these modules to patch a vanilla distribution
        of Wildfly Application Server.
    
  server/
        This directory contains a preconfigured distribution 
        of Wildfly Application Server with camunda BPM platform readily
        installed. 
        
        run the    
          server/wildfly-${version.wildfly10}/bin/standalone.{bat/sh}
        script to start up the the server.
        
        After starting the server, you can access the 
        following web applications:
        
        http://localhost:8080/camunda
        http://localhost:8080/engine-rest

    sql/
        This directory contains the create and upgrade sql script
        for the different databases.
        The engine create script contain the engine and history tables.
        
        Execute the current upgrade script to make the database compatible
        with the newest camunda BPM platform release.

==================
    
camunda BPM platform version: ${project.version}
Wildfly Application Server version: ${version.wildfly10}

=================
