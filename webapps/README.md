# Camunda Webapp

This is the Camunda Platform web application source.
Clean, package and install it via [Maven](https://maven.apache.org/).

## Structure of this project

The structure is as follows:

* `assembly` - Java sources and tests for the Camunda web application based on `javax` namespace.
* `assembly-jakarta` - Java sources and tests for the Camunda web application based on `jakarta` namespace.
  * This module is created from the `assembly` module via code transformation.
* `frontend` - HTML, CSS and Javascript sources as well as Plugins and tests for the Camunda webapplications Cockpit, Tasklist and Admin.

## FRONTEND

### UI

There are 3 web applications available for the Camunda Platform :

* __cockpit__: an administration interface for processes and decisions
* __tasklist__: provides an interface to process user tasks
* __admin__: is used to administer users, groups and their authorizations

The webapps above are relying on 2 libraries:

* __camunda-bpm-sdk-js__: provides tools for developers who want interact with the platform using Javascript
* __camunda-commons-ui__: is a set of shared scripts, templates and assets, used in the different webapps


#### Plugins

Parts of the web applications can be extended using plugins.

See [plugin development guide](http://docs.camunda.org/latest/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) for details.

#### Translations

English and german translations are located in the `ui/<app>/client/locales` folders.  
Translations for other languages are available in the [camunda-7-webapp-translations](https://github.com/camunda-community-hub/camunda-7-webapp-translations) repository.

### Libraries

#### [camunda-bpm-sdk-js](https://github.com/camunda/camunda-bpm-platform/tree/master/webapps/frontend/camunda-bpm-sdk-js)

Has tools to work with the REST API and forms (included transitively via camunda-commons-ui).

#### [camunda-commons-ui](https://github.com/camunda/camunda-bpm-platform/tree/master/webapps/frontend/camunda-commons-ui)

Contains resources like images, [`.less`](http://lesscss.org) stylesheets as well as some [angular.js](http://angularjs.org) modules.

### Prerequisite

You need [node.js](http://nodejs.org) >= 17 and npm.

### Setup

#### Adjusting Maven Settings

See https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md#build-from-source

#### Using Webpack

Build the web apps using Webpack:

```sh
# cd <path to your workspace>
git clone git@github.com:camunda/camunda-bpm-platform.git
cd camunda-bpm-platform/webapps/frontend
npm install
npm start
```

To start the server in development mode, call

```sh
cd camunda-bpm-platform/webapps/assembly
mvn jetty:run -Pdevelop
```

The webapps are then available pointing a browser at [http://localhost:8080](http://localhost:8080). To login as an admin user, use `jonny1` as username and password.

You can now start developing using the `npm run start` command in the frontend directory.

##### Jakarta Webapps

In order to run the Jakarta Webapps start Jetty the same way from the `assembly-jakarta` folder

```sh
cd camunda-bpm-platform/webapps/assembly
mvn jetty:run -Pdevelop
npm run start
```

#### AngularJS libraries from XLTS.dev

Since December 31, 2021, AngularJS is no longer officially supported by the original maintainers (Google). We replaced the official AngularJS libraries with the ones from [XLTS.dev](https://XLTS.dev) to ensure that our used libraries stay secure and supported. We include the AngularJS libraries from XLTS.dev in our Community Edition releases from 7.18.0-alpha2 on.

**Heads-up:** If you build the Webapps from source code, the build includes the no longer maintained AngularJS libraries in version 1.8.2 unless you have access to the XLTS.dev registry and configure it as shown below.

To enable pulling the XLTS.dev AngularJS libraries while building the Webapps, please configure the npm registry. Add the XLTS.dev npm registry by replacing the variables `${XLTS_REGISTRY}` and `${XLTS_AUTH_TOKEN}` in the following commands and execute the commands in your terminal.

Commands to configure the XLTS.dev npm registry:

```
npm set @xlts.dev:registry https://${XLTS_REGISTRY}/
npm set //${XLTS_REGISTRY}/:_authToken ${XLTS_AUTH_TOKEN}
```

Alternatively, you can set the following environment variables:

```sh
export XLTS_REGISTRY = "example.com" # Hostname without protocol (e.g., "https://"), leading or trailing slashes
export XLTS_AUTH_TOKEN = "abc..."    # Token to authenticate against the registry
```

You receive the information about the registry and the auth token directly from XLTS.dev.

## Browsers support

The supported browsers are:

- Chrome Latest
- Firefox Latest
- Edge Latest

## Contributing

Have a look at our [contribution guide](https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md) for how to contribute to this repository.


## Help and support

* [Documentation](http://docs.camunda.org/manual/latest/)
* [Forum](https://forum.camunda.org)
* [Stackoverflow](https://stackoverflow.com/questions/tagged/camunda)

## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).
