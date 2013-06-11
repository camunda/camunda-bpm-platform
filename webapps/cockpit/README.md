Cockpit
=======

This folder contains cockpit maven projects. Clean, package and install it via [Maven](https://maven.apache.org/).

The structure is as follows:

* `cockpit-core` - cockpit core application and plugin infrastructure
* `cockpit-webapp` - cockpit web application
* `cockpit-{container}` - projects that produce cockpit web application for the different bpm platform containers

Plugins
-------

Cockpit may be extended using plugins.
The aim of these plugins is to provide the application with additional views on process engines provided by a camunda BPM platform installation.

See [plugin development guide](http://docs.camunda.org/how-tos/cockpit/develop-a-plugin/) for details.