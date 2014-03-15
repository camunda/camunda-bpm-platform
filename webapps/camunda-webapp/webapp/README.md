# camunda web application

This folder contains the sources of the camunda web application. The application includes

-   __cockpit__, a process monitoring tool
-   __tasklist__, a simple task list
-   __admin__, a administration interface


## Building the Project

To develop the application you need [Maven](https://maven.apache.org/) and [Grunt](http://gruntjs.com).


### Development Setup

#### Installation



#### Development

To run the development setup you need to execute

```
mvn clean jetty:run -Pdevelop
```

This bootstraps [Jetty](http://www.eclipse.org/jetty/), an embedded web server that serves the application on [localhost:8080/camunda](http://localhost:8080/camunda).
You may configure the port the server runs on by passing the argument `-Djetty.port=WHICH_PORT` to the command line.

In a other shell execute

```
grunt auto-build
```

This continuously builds the web assets using [Grunt](http://gruntjs.com) and automatically reloads the web application when done.
If you changed the port Jetty runs on, expose that via the environment variable `APP_PORT=WHICH_PORT`.

Login credentials:
* Username: `jonny1`
* Password: `jonny1`




### Editor configuration

In order to keep the coding style consistant, we placed a `.editorconfig` file in this directory.
Download a plugin for the .editorconfig [support in your favourite IDE](http://editorconfig.org/#download).

As described in the `.editorconfig` file, we use an indentation of 2 spaces,
UNIX-like new lines (lf) and a new line at the and of file in the **.js**, **.less** and **.html** files.

#### Scripts

We started linting the **.js** files using [jshint](http://www.jshint.com/).

#### Styles

We use [less](http://less.github.io) to generate the CSS stylesheets.
The **.less** files are located in the `webapps/camunda-webapp/webapp/assets/styles` directory.


### Testing

You may need to expose the location of your browser executable(s) via
the environment variables `(PHANTOMJS|FIREFOX|CHROME|IE)_BIN` for the testing environement to be set correctly.

#### Server Side

```
mvn clean test
```

#### Client Side - _unit_

```
grunt test:unit
```

#### Client Side - _integration_

To test the integration, you will need to have a [running instance](#development-setup) (on, default, port 8080) of the camunda BPM platform.

```
grunt test:e2e
```

If you want to run a specific set of tests (available sets are: `admin`, `cockpit` and `tasklist`), you can run the following command:

```
grunt test:e2e:<set name>
```

If you want to work on E2E tests and run only 1 set of tests when a change is made,
you can do so by setting an environement variable `E2E_TESTS`.

```
E2E_TESTS=<set name> grunt auto-build
```


### Packaging

```
mvn clean (package|install)
```


## Build Tasks

### Grunt

There are a few [grunt tasks](http://gruntjs.com/) that aid you when developing the application.

-   Continuously rebuild the web resources on changes and [live reload](http://livereload.com/) the app when finished:

    ```
    grunt auto-build
    ```

    Configure the port used by live reload in the environment variable `LIVERELOAD_PORT=LIVE_RELOAD_PORT`.

-   Execute a single development build of the web resources and provide them to maven build / embedded [Jetty](http://www.eclipse.org/jetty/):

    ```
    grunt build
    ```

-   Execute a single production build of the web resources and provide them to the maven build / embedded server:

    ```
    grunt build:dist
    ```


## Additional Resources

### Extending the application through plugins

Parts of the application (read: _cockpit_ and _admin_) can be extended through plugins.

#### Extending Cockpit through plugins

Read more about [how cockpit plugins work and how to develop them](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) in the docs.

#### Extending Admin through plugins

Plugins to the Admin application work in the same way as cockpit plugins. The server side Java plugin SPI interface is `org.camunda.bpm.admin.plugin.spi.AdminPlugin`. 

On the client side, admin supports the following extension points: 

* `admin.system`: Plugins are added to the _System_ section and may provide a settings panel.

#### Plugin exclusion (Client Side)

You can exclude some plugins from the interface by adding a `cam-exclude-plugins`
attribute to the `base` tag of the page loading the interface.
The content of the attribute is a comma separated list formatted like: `<plugin.key>:<feature.id>`.
If the feature ID is not provided, the whole plugin will be excluded.

#### Excluding a complete plugin (Client Side)

This example will completely deactivate the action buttons on the right side of the process instance view.

```html
<base href="/"
      cam-exclude-plugins="cockpit.processInstance.runtime.action" />
```

#### Excluding a plugin feature (Client Side)

In this example, we deactivate the definition list in the cockpit dashboard
but keep the diagram previews and disable the job retry action button:

```html
<base href="/"
      cam-exclude-plugins="cockpit.dashboard:process-definition-tiles,
                           cockpit.processInstance.runtime.action:job-retry-action" />
```

#### Overriding a Plugin's Resources (Server Side)

It is possible for one plugin to override some of it's own or other plugin's static resources. 
Static resources are web resources like Html views, Java Script assets, CSS, Images ...

This feature is useful for many usecases such as customizing built-in plugins, securing plugins or 
deactivating plugins. 

In order to implement a resource override, a plugin must provide an implementation of the 
`org.camunda.bpm.webapp.plugin.resource.PluginResourceOverride` interface: 

```java
public class ExampleResourceOverride implements PluginResourceOverride {

  public InputStream filterResource(InputStream inputStream, RequestInfo requestInfo) {

    if(requestInfo.getUriInfo().getPath().endsWith("/path/to/asset/filename.html")) {
      return ExampleResourceOverride.class.getClassLoader().getResourceAsStream("override.html");

    } else {
      return inputStream;

    }
  }
}
```

An instance of the implementation can then be returned from a plugin class in the `public List<PluginResourceOverride> getResourceOverrides();` method. 

### Generate Documentation for the Application

You can also generated the documentation using [JSDoc](http://usejsdoc.org/) with the following command:

```
jsdoc -c ./jsdoc-conf.json -d doc
```

This will put the documentation into the `doc` folder.
