camunda Webapp
==============

This folder contains the camunda webapp maven projects.
Clean, package and install it via [Maven](https://maven.apache.org/).

The structure is as follows:

* `camunda-core` - camunda core application and plugin infrastructure
* `camunda-webapp` - camunda web application
* `camunda-{container}` - projects that produce camunda web application for the different bpm platform containers


Plugins
-------

Parts of the application may be extended using plugins.
The aim of these plugins is to provide the application with additional views on process engines provided by a camunda BPM platform installation.

See [plugin development guide](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) for details.
