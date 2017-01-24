# camunda Webapp

This is the camunda BPM webapplication source.
Clean, package and install it via [Maven](https://maven.apache.org/).

## Structure of this project

The structure is as follows:

* `ui` - HTML, CSS and Javascript sources as well as plugins and test for the Camunda webapplications Cockpit, Tasklist and Admin.
* `src` - Java sources and tests for the Camunda webapplication.


## UI

There are 3 webapplications available for the camunda BPM platform:

* __cockpit__: an administration interface for processes and decisions
* __tasklist__: provides an interface to process user tasks
* __admin__: is used to administer users, groups and their authorizations

The webapps above are relying on 2 libraries:

* __camunda-bpm-sdk-js__: provides tools for developers who want interact with the platform using Javascript
* __camunda-commons-ui__: is a set of shared scripts, templates and assets, used in the different webapps


#### Plugins

Parts of the webapplications can be extended using plugins.

See [plugin development guide](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) for details.


## Libraries

### [camunda-bpm-sdk-js](https://github.com/camunda/camunda-bpm-sdk-js)

Has tools to work with the REST API and forms (included transitively via camunda-commons-ui).

### [camunda-commons-ui](https://github.com/camunda/camunda-commons-ui)

Contains resources like images, [`.less`](http://lesscss.org) stylesheets as well as some [angular.js](http://angularjs.org) modules and locales for the translation of the Tasklist interface texts.


## Development

### Prerequisite

You need [node.js](http://nodejs.org) and npm. You will also need to install [grunt](http://gruntjs.com) globally using `npm install -g grunt-cli`.

### Setup

#### Adjusting Maven Settings

See https://github.com/camunda/camunda-bpm-platform#building-camunda-bpm-platform

#### Using grunt

Installing the webapps is done by grunt:

```sh
# cd <path to your workspace>
git clone git@github.com:camunda/camunda-bpm-webapp.git
cd camunda-bpm-webapp
npm install
grunt
```

To start the server in development mode, call

```sh
mvn jetty:run -Pdevelop
```
The webapps are then available pointing a browser at [http://localhost:8080](http://localhost:8080). To login as admin user, use `jonny1` as username and password.

You can now start developing using the `grunt auto-build` command in the webapp directory. To shorten compile times, you can specify the project you are going to make changes to by calling `grunt auto-build:cockpit`

If you are only changing Javascript files, you can set the environment variable `FAST_BUILD` to 1 to further improve compile times.

If you want to make changes in the camunda-commons-ui project or the camunda-bpm-sdk-js, you have to link the projects via npm:

```sh
# cd <path to your workspace>
git clone git@github.com:camunda/camunda-commons-ui.git
cd camunda-commons-ui
npm install
npm link
cd ../camunda-bpm-webapp
npm link camunda-commons-ui
```

#### Testing

Install the webapps with grunt and start the server in test mode:

```sh
mvn jetty:run -Pdev-e2e
```

Make sure that you terminate the server for development or use another port. You may configure the port the server runs on by passing the argument -Djetty.port=WHICH_PORT to the command line.

To run the tests, call

```sh
grunt test-e2e --protractorConfig=ui/common/tests/develop.conf.js
```

Now, it opens a new browser at [http://localhost:8080](http://localhost:8080) and do the tests steps. If you want to tests only one spec or a part of it then you can annotate the description of the spec with the keyword `only`:

```javascript
describe.only('Cockpit Dashboard Spec', function() {
  // ...
}
```

## Browsers support

The supported browsers are:

- Chrome
- Firefox
- Internet Explorer 9+


## Contributing

You are __more than welcome__ to take part on the development of this project!

### Issues

You can submit issues in the [camunda Jira](https://app.camunda.com/jira/issues/).

### Coding

Clone the repository, add, fix or improve and send us a pull request.
But please take care about the commit messages, [our conventions can be found
here](https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md).

### Coding style guide

In place of a guide, just follow the formatting of existing code (and / or use the [.editorconfig](http://editorconfig.org/) files provided).

## Help and support

* [Documentation](http://docs.camunda.org/latest/)
* [Forum](https://forum.camunda.org)
* [Stackoverflow](stackoverflow.com/questions/tagged/camunda)

## License

Unless otherwise specified this project is licensed under [Apache License Version 2.0](./LICENSE).
