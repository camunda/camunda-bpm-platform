# camunda web application

This folder contains the sources of the camunda web application. The application includes

-   __cockpit__, a process monitoring tool
-   __tasklist__, a simple task list
-   __admin__, a administration interface


## Building the Project

To develop the application you need [Maven](https://maven.apache.org/) and [Grunt](http://gruntjs.com). 


### Development Setup

To run the development setup you need to execute

```
mvn clean jetty:run -Pdevelop
```

This bootstraps [Jetty](http://www.eclipse.org/jetty/), an embedded web server that serves the application on [localhost:8080/camunda](http://localhost:8080/camunda).
You may configure the port the server runs on by passing the argument `-Djetty.port=WHICH_PORT` to the command line.

In another shell execute

```
grunt auto-build
```

This continuously builds the web assets using [Grunt](http://gruntjs.com) and automatically reloads the web application when done.
If you changed the port Jetty runs on, expose that via the environment variable `APP_PORT=WHICH_PORT`.


### Testing

#### Server Side

```
mvn clean test
```

#### Client Side

```
grunt test
```

You may need to expose the location of your browser executable(s) via the environment variables `(PHANTOMJS|FIREFOX|CHROME|IE)_BIN`.


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

### Extending the Application through Plug-ins

Parts of the application (read: _cockpit_) can be extended through plug-ins.
Read more about [cockpit plug-ins and how to develop them](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) in the [docs](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin).

### Generate Documentation for the Application

You can also generated the documentation using [JSDoc](http://usejsdoc.org/) with the following command:

```
jsdoc -c ./jsdoc-conf.json -d doc
```

This will put the documentation into the `doc` folder.