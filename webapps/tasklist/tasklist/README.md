tasklist webapp
===============

The real tasklist webapplication.


Development Setup
-----------------

For developing the tasklist you can use the `develop` profile.
It will bootstrap the tasklist in an embedded tomcat and allows it to reload web resources on the fly.

The tasklist on embedded tomcat can be started from within this folder via `mvn clean tomcat:run -Pdevelop`