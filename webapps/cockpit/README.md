Cockpit
=======

This folder contains cockpit maven projects.

The structure is as follows:

* `cockpit-core` - cockpit core application and plugin infrastructure
* `cockpit-webapp` - cockpit web application
* `cockpit-{container}` - projects that produce cockpit web application for the different bpm platform containers


Development Setup
-----------------

For developing cockpit you can use the `dev` profile.
It will bootstrap cockpit in an embedded tomcat and allows it to reload web resources on the fly.

The cockpit on embedded tomcat can be started from within the `cockpit-webapp` folder via `mvn clean tomcat:run -Pdev`.


Plugins
-------

Cockpit may be extended using plugins.
The aim of these plugins is to provide the application with additional views on process engines provided by a camunda BPM platform installation.

For that purpose, plugins may provide engine queries (using [MyBatis](http://www.mybatis.org/)),
server side REST-resources (using [JAX-RS](https://jax-rs-spec.java.net/)) as well as client side resources that extend the cockpit client application.

### Structure of a Plugin

A cockpit plugin is a maven jar project that is written against the `org.camunda.bpm.cockpit:cockpit-core` library.
It [publishes itself](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) through the
service provider interface (SPI) `org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin`.

Via the SPI a plugin publishes

* a unique name
* a number of JAX-RS classes that provide the server side api of the plugin
* a path to the plugins client side assets
* a number of MyBatis database queries that may be used in server side resources

#### Plugin core dependencies

A plugin should depend on the library `org.camunda.bpm.cockpit:camunda-cockpit-core`.

#### Recommendations

A plugin _should_ provide a root resource class which extends `org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource`.
That resource _should_ bind to the path `plugin/$pluginId`, configured through a `@Path` annotation.

A plugins client side _must_ provide a main file `app/plugin.js` in its asset directory that declares itself and its dependencies.
In that file, the plugin _may_ declare client side plugin views and configure cockpit.

#### Sample packaging

Packaged in a jar file that typically results in a archive such as the following

```
META-INF/services/org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin    // contains the spi implementation class
org/mysample/cockpit/plugin/SamplePlugin.class    // compiled sample plugin
org/mysample/cockpit/plugin/resources/SamplePluginRootResource.class    // rest api provided by the plugin
org/mysample/cockpit/plugin/resources/SamplePluginResource.class    // implementation of a custom resource
org/mysample/cockpit/plugin/queries/queries.xml    // mybatis mappings for custom database queries provided by the plugin
org/mysample/cockpit/plugin/assets/info.txt    // optional readme file
org/mysample/cockpit/plugin/assets/app/    // location of the client side plugin implementation
org/mysample/cockpit/plugin/assets/app/plugin.js    // client side plugin definition
```


