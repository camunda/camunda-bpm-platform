Cockpit
=======

This folder contains cockpit maven projects.

The structure is as follows:

* `cockpit-core` - cockpit core application and plugin infrastructure
* `cockpit-webapp` - cockpit web application
* `cockpit-{container}` - projects that produce cockpit web application for the different bpm platform containers


Development Setup
-----------------

For developing cockpit you can use an embedded tomcat that can be run via `mvn clean tomcat:run -Pdev` from within the `cockpit-webapp` folder.
In this setup cockpit bootstraps an embedded engine and reloading of resources is possible.


Plugins
-------

Cockpit may be extended using plugin-jars.
Those jars can contain server-side resources (such as REST interfaces)
as well as client-side resources (html views and javascript).


### Structure of a Cockpit Plugin

A cockpit plugin publishes itself via a `META-INF/services` implementation
of the type `org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin`.

A plugin _should_ provide a root resource class which extends `org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource`.
That resource _should_ bind to the path `plugin/$pluginId`, configured through a `@Path` annotation.

A plugins client side _must_ provide a main file `app/plugin.js` in its asset directory that declares itself and its dependencies.
In that file, the plugin _may_ declare client side plugin views and configure cockpit.

#### Sample packaging

Packaged in a jar file that typically results in a archive such as the following

```
META-INF/services/org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin    // contains the plugin implementation class
org/mysample/cockpit/plugin/SamplePlugin.class    // compiled sample plugin
org/mysample/cockpit/plugin/resources/SamplePluginRootResource.class    // rest api provided by the plugin
org/mysample/cockpit/plugin/resources/SamplePluginResource.class    // implementation of a custom resource
org/mysample/cockpit/plugin/queries/queries.xml    // mybatis mappings for custom database queries provided by the plugin
org/mysample/cockpit/plugin/assets/info.txt    // optional readme file
org/mysample/cockpit/plugin/assets/app/    // location of the client side plugin implementation
org/mysample/cockpit/plugin/assets/app/plugin.js    // client side plugin definition
```


