# camunda tasklist

A web based interface for [camunda BPM platform](http://camunda.org) tasks.

## Installation

`git clone git@github.com:camunda/camunda-tasklist-ui.git camunda-tasklist-ui && cd $_ && npm install`

## Usage

The project itself is aimed to be served (statically) by a web server.
You just need to (optionally) build the project and serve its `dist` folder.

## Development

The most convenient way to work on the tasklist development is to have it (linked) in the BPM platform.
This will allow live reloading from within the platform served web UI (and significantly ease your work). 

__Note:__ you need to have [npm](http://npmjs.org) (the [node.js](http://nodejs.org/) package manager) installed in order to complete the following instructions.

```bash
# clone the camunda BPM platform project
git clone git@github.com:camunda/camunda-bpm-platform.git

# clone the camunda tasklist project
git clone git@github.com:camunda/camunda-tasklist-ui.git

# create a npm link
cd camunda-tasklist-ui
npm link
cd ..

# go in the platform webapp directory
cd camunda-bpm-platform/webapps/camunda-webapp/webapp

# replace the tasklist module by its link
rm node_modules/camunda-tasklist-ui
npm link camunda-tasklist-ui

# build with maven (the default port is 8080) and start serving
mvn clean jetty:run -Djetty.port=9090 -Pdevelop
```

Using a __second terminal session__, run the auto-build for the platform

```bash
# go in the platform webapp directory
cd camunda-bpm-platform/webapps/camunda-webapp/webapp

# run the automated build, with livereload using port 9091
LIVERELOAD_PORT=9091 grunt auto-build
# alternatively, if you do not have grunt-cli installed globally,
# grunt executable can also be found in node_modules/.bin/grunt
```

With a __third terminal session__, run the build tools for the tasklist

```bash
# go in the platform webapp directory
cd camunda-tasklist-ui

# run the automated build
grunt auto-build
```

Now, you should be able to access the tasklist:

 - either from within the platform at http://localhost:9090/camunda/app/tasklist/default/
 - or the statically served (served by grunt connect) version: http://localhost:7070

and if you make changes in the content of the tasklist `client` directory, both of the addresses above should reload automatically.


### Contributing

You are __more than welcome__ to take part on the development of this project!

Clone the repository, add, fix or improve and send us a pull request.    
But please take care about the commit messages, [our conventions can be found
here](https://github.com/ajoslin/conventional-changelog/blob/master/CONVENTIONS.md).


## Browsers support

The supported browsers are:
 - Chrome
 - Firefox
 - Internet Explorer 9+

## Test

They are 3 "testing methods" provide for development.

### Unit tests

Is the fastest executed testing suite, perfect for testing objects
(who do not rely on something normally provided by a browser).   
You can run the unit tests with `grunt jasmine_node`.

### Integration tests

A little slower but allows to test things in browsers.   
You can run the unit tests with `grunt karma`.

### E2E tests

Very slow and especially annoying to write tests for, install and maintain
(OK, OK, personal point of view), writing E2E tests is still one of
the best ways to test the application in its whole.   
You can run the unit tests with `grunt build connect protractor`.

## Coding styleguide

In place of a guide, just follow the formatting of existing code.

## License

The camunda tasklist is licensed under [Apache License Version 2.0](./LICENSE).

## Authors

 - [Valentin _zeropaper_ Vago](https://github.com/zeropaper) - [@zeropaper](http://twitter.com/zeropaper)
 - [Nico _Nikku_ Rehwaldt](https://github.com/nikku) - [@nrehwaldt](http://twitter.com/nrehwaldt) 
 - [Daniel _meyerdan_ Meyer](https://github.com/meyerdan) - [@meyerdan](http://twitter.com/meyerdan)