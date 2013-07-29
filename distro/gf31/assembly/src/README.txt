This is a distribution of 

       camunda BPM platform v${project.version}
          
visit     
       http://docs.camunda.org/

   
camunda BPM platform is licensed under the Apache License v2.0
http://www.apache.org/licenses/LICENSE-2.0
 
The packaged glassfish server is licensed under the LGPL license.

==================

Contents:

  client/
        This directory contains the client jar which is 
        needed for deploying process applications to the 
        camunda BPM platform.
  
  modules/
        This directory contains the modules which 
        make up the camunda BPM platform. You can use these
        modules and copy them to a vanilla distribution of
        glassfish.
          
  server/
        This directory contains a preconfigured distribution 
        of glassfish 3.1.x with camunda BPM platform readily installed. 
        
        run the         
                server/glassfish3/glassfish/bin/startserv.{bat/sh} 
        script to start up the the server.
          
        After starting the server, you can access the 
        following web applications:
        
        http://localhost:8080/camunda
        http://localhost:8080/engine-rest
        http://localhost:8080/cycle

  sql/
        This directory contains the create and upgrade sql script
        for the different databases.
        The engine create script contain the engine and history tables.
        
        Execute the current upgrade script to make the database compatible
        with the newest camunda BPM platform release.

==================
    
camunda BPM platform version: ${project.version}
Glassfish Application Server version: ${version.glassfish}

=================
