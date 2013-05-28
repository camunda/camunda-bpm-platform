Cockpit
=======

This folder contains cockpit maven projects. Clean, package and install it via [Maven](https://maven.apache.org/).

The structure is as follows:

* `cockpit-core` - cockpit core application and plugin infrastructure
* `cockpit-webapp` - cockpit web application
* `cockpit-{container}` - projects that produce cockpit web application for the different bpm platform containers


Development Setup
-----------------

For developing cockpit you can use the `dev` profile.
It will bootstrap cockpit in an embedded tomcat and allows it to reload web resources on the fly.

The cockpit on embedded tomcat can be started from within the `cockpit-webapp` folder via `mvn clean tomcat:run -Pdev`.


Test Suite
----------


### cockpit-core

Run test suite via `mvn clean test`.


### cockpit-webapp

Run server side tests via `mvn clean test`.

Client side tests require [karma](http://karma-runner.github.com) >= 0.9. Install using `npm -g install karma@canary`.
Run client side tests via `karma start src/test/js/config/karma.unit.js`.
Run client side end-to-end tests via `karma start src/test/js/config/karma.e2e.js` (requires dev environment to be running).


Plugins
-------

Cockpit may be extended using plugins.
The aim of these plugins is to provide the application with additional views on process engines provided by a camunda BPM platform installation.

See [plugin development guide](http://docs.camunda.org/how-tos/cockpit/develop-a-plugin/) for details.