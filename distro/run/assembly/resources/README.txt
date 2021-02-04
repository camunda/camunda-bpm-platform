This is a distribution of

       Camunda Platform v${project.version}

visit
       http://docs.camunda.org/

The Camunda Platform is a dual-license Java-based framework.
This particular copy of the Camunda Platform is released either
under the Apache License 2.0 (Community Platform) OR a commercial
license agreement (Enterprise Platform).

License information can be found in the LICENSE file.
 
The Camunda Platform includes libraries developed by third
parties. For license and attribution notices for these libraries,
please refer to the documentation that accompanies this distribution
(see the LICENSE_BOOK-${project.version} file).

The packaged Apache Tomcat server is licensed under 
the Apache License v2.0 license.

==================

Contents:

  /
        The root directory contains two start scripts. One for Windows (.bat)
        and one for Linux/Mac (.sh). After executing it, you can access the 
        following web applications:

        webapps: http://localhost:8080/
        rest: http://localhost:8080/engine-rest/

  internal/
        This directory contains the Java application and optional components
        that Camunda Platform Run consists of.

  configuration/
        This directory contains all resources to configure the distro.
        Find a detailed guide on how to use this directory on the following
        documentation pages:
        https://docs.camunda.org/manual/latest/installation/camunda-bpm-run/
        https://docs.camunda.org/manual/latest/user-guide/camunda-bpm-run/

==================

Camunda Platform version: ${project.version}

=================
