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

We started linting the **.js** files using [jshint](http://www.jshint.com/) and we `'use strict';`
because it is a __[good thing to do](http://www.webdesignporto.com/why-use-strict-in-javascript-can-save-you-hours)__.

To improve the readability (and code reviews) of the code we also suggest to use new lines for:

 - __objects__ or __arrays__ with more than 1 key
 - __statements__ like:
    - __if__, __else__, __else if__
    - __try / catch__

__Avoid:__
```js
var array_of_values = ["first", "second", 3, "fifth"];
var obj = {"key": "string value in double-quotes", other: "double quotes are for HTML", or_something_else: 10};
if (condition == false) {
    //...
}else if(other_condition == true){
    try{
        //...
    }catch(e){
        //...
    }
}
```

__Instead:__
```js
var arrayOfValues = [
    'first',
    'second',
    3,
    'fifth'
];

var obj = {
    key: 'string value',
    other: 'single quotes are for JS',
    andVariableNamesAreCamelCased: 10
};

if (condition) {
    //...
}
else if (otherCondition) {
    try{
        //...
    }
    catch(err){
        //...
    }
}
```

_Note_: They are tools available, like the [JSHint Gutter](https://github.com/victorporof/Sublime-JSHint) plugin,
who will help a lot spotting the linting issues.

#### HTML

To ease the comparaison in diffs, except for the first and expect when there is only 1,
the attributes of a tag should be placed on a seperate line.

An other good thing to do to ease the development is to put a comment (with the location )
at the beginning and end of the HTML files used as angular templates, as follow:

```html
<!-- # CE src/path/to/the/file.html -->
<div first-attribute-on-same-line
     additional-attributes-on="new lines"
     and-so-on>
  ...
</div>
<!-- / CE src/path/to/the/file.html -->
```

The `#` implies here the beginning and the `/` implies the end of the template. The `CE` is meant to
indicate that that the file is part of the community edition of the platform.
It makes much easier to find which file is used during development.

_Note:_ those comments are removed when the project is build for production.

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

#### Client Side - _integration_

To test the integration you can run:

```
mvn clean verify -P test-e2e
```

If you want to develop e2e test with live code reload, you will need to have a running instance of the test backend of the camunda BPM platform:

```
mvn jetty:run -P dev-e2e
```

After that you can run the integration test:

```
grunt test-e2e
```

If you want to run a specific protractor config:

```
grunt test-e2e --protractorConfig src/test/js/e2e/my.conf.js
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

-   Only build a single webapp continuously to speed up compile times:

    ```
    grunt auto-build:cockpit
    ```

-   Execute a single production build of the web resources and provide them to maven build / embedded [Jetty](http://www.eclipse.org/jetty/):

    ```
    grunt build
    ```

-   Execute a single development build of the web resources and provide them to the maven build / embedded server:

    ```
    grunt build:dev
    ```

-   Execute a single development build of a single web application and provide it to the maven build / embedded server:

    ```
    grunt build:dev:cockpit
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
<base href="$BASE"
      cockpit-api="$APP_ROOT/api/cockpit/"
      engine-api="$APP_ROOT/api/engine/"
      app-root="$APP_ROOT"
      cam-exclude-plugins="cockpit.processInstance.runtime.action" />
```

#### Excluding a plugin feature (Client Side)

In this example, we deactivate the definition list in the cockpit dashboard
but keep the diagram previews and disable the job retry action button:

```html
<base href="$BASE"
      cockpit-api="$APP_ROOT/api/cockpit/"
      engine-api="$APP_ROOT/api/engine/"
      app-root="$APP_ROOT"
      cam-exclude-plugins="cockpit.dashboard:process-definition-tiles,
                           cockpit.processInstance.runtime.action:job-retry-action" />
```

__Important__: make sure you only keep 1 `base` tag.

_Hint_: If you need to know what plugins and features are available and/or excluded, open your browser
developer tools (generally by hiting F12) and, in the console, enter:
```javascript
angular.module('cockpit.plugin')._camPlugins
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
