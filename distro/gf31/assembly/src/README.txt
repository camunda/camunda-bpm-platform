This is a distribution of the 

   camunda fox bpm platform enterprise edition (ee)

camunda fox ee, (c) camunda services GmbH
 
Packaged glassfish server is licensed under the LGPL license.

==================

Contents:

        client/
                This directory contains the client jar which is 
                needed for deploying process applications to the 
                fox platform.
        
        modules/
                This directory contains the modules which 
                make up the fox platform. You can use these modules 
                and copy them to a vanilla distribution of glassfish.
                
        server/
                This directory contains a preconfigured distribution 
                of glassfish 3.1.x with camunda fox ee readily installed. 
                
                run the         
                        server/glassfish3/glassfish/bin/startserv.{bat/sh} 
                script to start up the the server.
                
                The server bundles a distribution of the activiti 
                task explorer (branded as fox-explorer).
                You can access it using the following URL:
                
                http://localhost:8080/explorer 

==================              
                
camunda fox ee version: ${project.version}
glassfish server version: ${version.glassfish}

==================
