# camunda Webapp

This is the camunda BPM webapplication backend and assembly.
Clean, package and install it via [Maven](https://maven.apache.org/).

## Structure of this project

The structure is as follows:

* `core` - camunda core application and plugin infrastructure
* `webapp` - camunda web application
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

### JS SDK

Has tools to work with the REST API and forms.

### Commons

Contains resources like images and [`.less`](http://lesscss.org) stylesheets as well as some [angular.js](http://angularjs.org) modules.

## Development

### Prerequisite

You need [node.js](http://nodejs.org) - we recommend using [nvm](https://github.com/creationix/nvm#install-script) to install node.js.
Optionally you could globally install [grunt](http://gruntjs.com/) (using `npm install -g grunt-cli`).

### Setup

Depending on your needs, you might want to clone the following repositories:

```sh
# cd <path to your workspace>

git clone git@github.com:camunda/camunda-bpm-webapp.git
git clone git@github.com:camunda/camunda-commons-ui.git
git clone git@github.com:camunda/camunda-bpm-sdk-js.git
git clone git@github.com:camunda/camunda-tasklist-ui.git
git clone git@github.com:camunda/camunda-admin-ui.git
git clone git@github.com:camunda/camunda-cockpit-ui.git
git clone git@github.com:camunda/camunda-cockpit-plugin-base.git
```

To ease development and provide live-reloading, you can link the projects as follow:

```sh
# cd <path to your workspace>

cd camunda-bpm-sdk-js
npm link
cd ..

cd camunda-commons-ui
npm link
cd ..

cd camunda-cockpit-ui
npm link
npm link camunda-commons-ui
npm link camunda-bpm-sdk-js
cd ..

cd camunda-tasklist-ui
npm link
npm link camunda-commons-ui
npm link camunda-bpm-sdk-js
cd ..

cd camunda-admin-ui
npm link
npm link camunda-commons-ui
npm link camunda-bpm-sdk-js
cd ..
```

__Note__: if you do not link the projects, you will have to run `npm install` in each of them.


From the `camunda-bpm-webapp` directory:
```sh
# might (or not) be needed
cd ../camunda-cockpit-plugin-base
mvn clean install
cd ../camunda-bpm-webapp
```

```sh
cd webapp
mvn clean install jetty:run -Pdevelop,livereload
```
The webapps are then available pointing a browser at [http://localhost:8080](http://localhost:8080)

You can now start developing using the `./node_modules/grunt-cli/bin/grunt auto-build` command in the directories (you need a separate terminal/process for each of them)

* `camunda-cockpit-ui`
* `camunda-tasklist-ui`
* `camunda-admin-ui`
* `camunda-commons-ui`
* `camunda-bpm-sdk-js`

If you made the linking, the pages in your browser should reload when a change is made to the scripts of those projects.



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

## License

Unless otherwise specified this project is licensed under [Apache License Version 2.0](./LICENSE).
