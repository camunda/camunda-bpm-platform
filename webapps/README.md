camunda Webapp
==============

This is the camunda BPM webapplication backend and assembly.
Clean, package and install it via [Maven](https://maven.apache.org/).

The structure is as follows:

* `core` - camunda core application and plugin infrastructure
* `webapp` - camunda web application
* `distro/{container}` - projects that produce camunda web application for the different bpm platform containers



```sh
git clone git@github.com:camunda/camunda-bpm-sdk-js.git
git clone git@github.com:camunda/camunda-tasklist-ui.git
git clone git@github.com:camunda/camunda-cockpit-ui.git
git clone git@github.com:camunda/camunda-commons-ui.git
git clone git@github.com:camunda/camunda-cockpit-base-plugin.git
git clone git@github.com:camunda/camunda-bpm-webapp.git
```


### cockpit terminal
From the `camunda-cockpit-ui` directory
```sh
LIVERELOAD_PORT=8082 grunt auto-build -target=dist/app/cockpit
```


### tasklist terminal
From the `camunda-tasklist-ui` directory
```sh
LIVERELOAD_PORT=8081 grunt auto-build -target=dist/app/tasklist
```


### webapp terminal
From the `camunda-bpm-webapp` directory
```sh
// might (or not) be needed
cd ../camunda-cockpit-plugin-base
mvn clean install
cd ../camunda-bpm-webapp
```

```sh
cd webapp
mvn clean install jetty:run -Pdevelop
```





Plugins
-------

Parts of the application may be extended using plugins.
The aim of these plugins is to provide the application with additional views on process engines provided by a camunda BPM platform installation.

See [plugin development guide](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) for details.
