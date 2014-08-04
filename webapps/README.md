# camunda tasklist

A web based interface for [camunda BPM platform](http://camunda.org) tasks.

## Installation

`git clone git@github.com:camunda/camunda-tasklist-ui.git camunda-tasklist-ui && cd $_ && npm install`

## Usage

The project itself is aimed to be served (statically) by a web server.
You just need to (optionally) build the project and serve its `dist` folder.

## Development

Development on the tasklist UI should be done using the [camunda-bpm-webapp](https://github.com/camunda/camunda-bpm-webapp/). Please refer to the development instructions of that project.

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

_needs update_
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

Unless otherwise specified, this project is licensed under [Apache License Version 2.0](./LICENSE).

## Authors

 - [Valentin _zeropaper_ Vago](https://github.com/zeropaper) - [@zeropaper](http://twitter.com/zeropaper)
 - [Nico _Nikku_ Rehwaldt](https://github.com/nikku) - [@nrehwaldt](http://twitter.com/nrehwaldt) 
 - [Daniel _meyerdan_ Meyer](https://github.com/meyerdan) - [@meyerdan](http://twitter.com/meyerdan)
