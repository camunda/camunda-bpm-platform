# camunda tasklist

A web based interface for [camunda BPM platform](http://camunda.org) tasks.

## Installation

`git clone git@github.com:camunda/camunda-tasklist.git camunda-tasklist && cd $_ && npm install`

## Usage

The project itself is aimed to be served (statically) by a web server.
You just need to (optionally) build the project and serve its `dist` folder.

## Development

You can start the development environement with the `grunt serve` command.


### Contributing

You are more than welcome to take part on the development of this project!

Clone the repository, add, fix or improve and send us a pull request.    
But please take care about the commit messages, [our conventions can be found
here](https://github.com/ajoslin/conventional-changelog/blob/master/CONVENTIONS.md).


## Browsers support

The supported browsers are:
 - Chrome
 - Firefox
 - Internet Explorer 9+

## Test

### Unit tests

~~You can run the unit tests with `grunt jasmine_node`.~~

Actually, no. Because only very specific parts of the code
(due to the dependencies) can be tested on the "node side".    
This might be a future feature but for now the 2 other testing
environments, although slower, should be enough.

### Integration tests

You can run the unit tests with `grunt karma`.

### E2E tests

You can run the unit tests with `grunt build connect protractor`.

## Coding styleguide

In place of a guide, just follow the formatting of existing code.

## License

The camunda tasklist is licensed under [Apache License Version 2.0](./LICENSE).

## Authors

[Valentin `zeropaper` Vago](https://github.com/zeropaper)
[Nico `Nikku` Rehwaldt](https://github.com/nikku)