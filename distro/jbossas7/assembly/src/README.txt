This is a distribution of

       Camunda BPM platform v${project.version}

visit
       http://docs.camunda.org/

The Camunda BPM platform is a dual-license Java-based framework.
This particular copy of the Camunda BPM Platform is released either
under the Apache License 2.0 (Community Platform) OR a commercial
license agreement (Enterprise Platform).

License information can be found in the LICENSE file.

The Camunda BPM platform includes libraries developed by third
parties. For license and attribution notices for these libraries,
please refer to the documentation that accompanies this distribution
(see the LICENSE_BOOK-${project.version} file).

The packaged JBoss Application Server 7 server is licensed under 
the LGPL license.

==================

Contents:

  lib/
        This directory contains the java libraries for application 
        development.
  
  modules/
        This directory contains additional modules for JBoss Application 
        erver 7. You can use these modules to patch a vanilla distribution 
        of JBoss Application Server.

  server/
        This directory contains a preconfigured distribution 
        of JBoss Application Server 7 with Camunda BPM platform readily 
        installed. 

        run the
          server/jboss-as-${version.jboss.as}/bin/standalone.{bat/sh} 
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
        with the newest Camunda BPM platform release.

==================

Camunda BPM platform version: ${project.version}
JBoss Application Server version: ${version.jboss.as}

=================
