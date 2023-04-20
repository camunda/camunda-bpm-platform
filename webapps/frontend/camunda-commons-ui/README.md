# camunda-commons-ui

Common frontend / UI resources, widgets and libraries for the [Camunda web application][webapp]:

> **Important note:**
> This project is used internally and the API of its components are subject to changes at any time.

## Content

- `lib`
  - `auth` - for authentication mechanisms and tools, [read more](./lib/auth/README.md)
  - `util` - commonly used utilities [read more](./lib/util/README.md)
  - `directives`
  - `pages`
  - `chart`
  - `plugin`
  - `resources`
  - `search`
  - `services`
- `resources`
  - `locales` - translation files
  - `img`
  - `less` - base less files to generate CSS stylesheets
- [`widgets`](#widgets)


### Widgets

Widgets are reusable components which should be easy to integrate in the [Camunda webapp][webapp] and your own projects.

#### Usage

A good way to get familiar with the widgets integration in your projects is by reading the source code of the `lib/widgets/*/test/*.spec.html`.
In those examples, we use uncompiled versions of the library and its dependencies and wire the whole with [requirejs](//requirejs.org).


#### Available widgets

- `cam-widget-bpmn-viewer`
- `cam-widget-cmmn-viewer`
- `cam-widget-dmn-viewer`
- `cam-widget-debug`
- `cam-widget-footer`
- `cam-widget-header`
- `cam-widget-inline-field`
- `cam-widget-loader`
- `cam-widget-chart-line`
- `cam-widget-search`
- `cam-widget-search-pill`
- `cam-widget-variable`
- `cam-widget-variables-table`
- `cam-widget-clipboard`
- `cam-widget-var-template`

#### Developing the widgets

```sh
grunt auto-build
```

#### Testing the widgets

```sh
npm install
./node_modules/grunt-protractor-runner/node_modules/protractor/bin/webdriver-manager --chrome update
grunt
```

While developing widgets, you may want to run the tests as a change occurs, here is a way to achieve that:
```sh
npm install -g nodemon
nodemon -w lib/widgets/ --exec "protractor ./test/protractor.conf.js"
```

You can also run the tests on a single widget like that:
```
TESTED=variable nodemon -w lib/widgets/ --exec "protractor ./test/protractor.conf.js"
```
This will only run the `cam-widget-variable` tests.

## grunt connect keep alive web server
```
grunt connect:widgetTests:keepalive
```

## Test

```sh
grunt karma
```

### Testing the widgets under macOS

Add to protractor.config.js:
```
directConnect: true,
```
Run in commons-ui folder:
```
rm -rf node_modules
npm install —-legacy-bundling
```
Open webdriver-manager and change mac32.zip to mac64.zip if you run a newer macOS version.
```
vim /camunda-commons-ui/node_modules/protractor/bin/webdriver-manager
```
Run chrome update for protractor
```
camunda-commons-ui/node_modules/protractor/bin/webdriver-manager --chrome update
```



## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).


## Authors

 - [Daniel _meyerdan_ Meyer](https://github.com/meyerdan) - [@meyerdan](http://twitter.com/meyerdan)
 - [Valentin _zeropaper_ Vago](https://github.com/zeropaper) - [@zeropaper](http://twitter.com/zeropaper)
 - [Nico _Nikku_ Rehwaldt](https://github.com/nikku) - [@nrehwaldt](http://twitter.com/nrehwaldt)
 - [Sebastian Stamm](https://github.com/SebastianStamm) - [@seb_stamm](https://twitter.com/seb_stamm)

[webapp]: https://github.com/camunda/camunda-bpm-platform/tree/master/webapps
