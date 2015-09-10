# camunda Webapp

This is the camunda BPM webapplication backend and assembly.
Clean, package and install it via [Maven](https://maven.apache.org/).

## Structure of this project

The structure is as follows:

* `core` - camunda core application and plugin infrastructure
* `webapp` - camunda web application
* `webjar` - stripped down webjar for use in embedded containers (like spring boot)
* `distro/{container}` - projects that produce camunda web application for the different bpm platform containers

## Webapps

They are 3 webapps available for the camunda BPM platform, they are:

* [__cockpit__](#cockpit): is an administration interface for the processes
* [__tasklist__](#tasklist): provides an interface to process user tasks
* [__admin__](#admin): is used to administer users, groups and their authorizations

The webapps above are relying on 2 libraries:

* [__JS SDK__](#js-sdk): provides tools for developers who want interact with the platform using Javascript
* [__commons__](#commons-ui): is a set of shared scripts, templates and assets, used in the different webapps

### Cockpit



#### Plugins

Parts of the cockpit application may be extended using plugins.
The aim of these plugins is to provide the application with additional views on process engines provided by a camunda BPM platform installation.

See [plugin development guide](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) for details.

### Tasklist



### Admin


## Libraries

### [JS SDK](https://github.com/camunda/camunda-bpm-sdk-js)

Has tools to work with the REST API and forms.

### [Commons](https://github.com/camunda/camunda-commons-ui)

Contains resources like images, [`.less`](http://lesscss.org) stylesheets as well as some [angular.js](http://angularjs.org) modules and locales for the translation of the Tasklist interface texts.

## Development

### Prerequisite

You need [node.js](http://nodejs.org) - we recommend using [nvm](https://github.com/creationix/nvm#install-script) to install node.js.
You will also need to install [grunt](http://gruntjs.com) globally using `npm install -g grunt-cli`.

### Setup

#### Adjusting Maven Settings

See https://github.com/camunda/camunda-bpm-platform#building-camunda-bpm-platform

#### Using grunt

Installing the webapps is done by grunt:

```sh
# cd <path to your workspace>
git clone git@github.com:camunda/camunda-bpm-webapp.git
cd camunda-bpm-webapp/webapp
npm install
grunt
```

#### Development Setup

In order to make changes to the webapps, you have to clone the repositories you need:

```sh
# cd <path to your workspace>

git clone git@github.com:camunda/camunda-bpm-webapp.git
git clone git@github.com:camunda/camunda-commons-ui.git
git clone git@github.com:camunda/camunda-bpm-sdk-js.git
git clone git@github.com:camunda/camunda-tasklist-ui.git
git clone git@github.com:camunda/camunda-admin-ui.git
git clone git@github.com:camunda/camunda-cockpit-ui.git
```

You can then link the projects so that changes are automatically picked up by the server. This example shows how to link the cockpit webapp:

```sh
# cd <path to your workspace>

cd camunda-cockpit-ui
npm link

cd ../camunda-bpm-webapp/webapp
npm link camunda-cockpit-ui
```

If you want to make changes in the camunda-commons-ui project or the camunda-bpm-sdk-js, you have to link the projects to the webapps itself (i.e. camunda-cockpit-ui --> camunda-commons-ui --> camunda-bpm-sdk-js).

To start the server in development mode, call

```sh
cd camunda-bpm-webapp/webapp
mvn clean jetty:run -Pdevelop
```
The webapps are then available pointing a browser at [http://localhost:8080](http://localhost:8080)

You can now start developing using the `grunt auto-build` command in the webapp directory. To shorten compile times, you can specify the project you are going to make changes to by calling `grunt auto-build:cockpit`

If you do not want the server to perform a frontend build on startup, because you are running your own grunt build during development, you can start the server with

```sh
mvn jetty:run -Pdevelop,skipFrontendBuild
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
here](https://github.com/ajoslin/conventional-changelog/blob/master/CONVENTIONS.md).

### Coding style guide

In place of a guide, just follow the formatting of existing code (and / or use the [.editorconfig](http://editorconfig.org/) files provided).

## Help and support

* [Documentation](http://docs.camunda.org/latest/)
* [Stackoverflow](stackoverflow.com/questions/tagged/camunda)
* Google groups for [users](https://groups.google.com/forum/#!forum/camunda-bpm-users) and [developers](https://groups.google.com/forum/#!forum/camunda-bpm-dev)

## Authors

- [Daniel _meyerdan_ Meyer](https://github.com/meyerdan) - [@meyerdan](http://twitter.com/meyerdan)
- [Valentin _zeropaper_ Vago](https://github.com/zeropaper) - [@zeropaper](http://twitter.com/zeropaper)
- [Nico _Nikku_ Rehwaldt](https://github.com/nikku) - [@nrehwaldt](http://twitter.com/nrehwaldt)
- [Sebastian Stamm](https://github.com/SebastianStamm) - [@seb_stamm](https://twitter.com/seb_stamm)

## License

Unless otherwise specified this project is licensed under [Apache License Version 2.0](./LICENSE).
