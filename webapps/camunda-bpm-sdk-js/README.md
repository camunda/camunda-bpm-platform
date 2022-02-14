# camunda-bpm-sdk-js

Javascript client library for [Camunda Platform](https://github.com/camunda/camunda-bpm-platform)

## Install using bower

```sh
bower install camunda-bpm-sdk-js --save
```

## Documentation

See https://docs.camunda.org/manual/latest/reference/embedded-forms/

## Development

```sh
npm install
```

```sh
grunt auto-build
```

### Testing

#### Karma

```sh
grunt karma
```

Alternatively, you can use the specific targets

```sh
grunt karma:dev-form
# or
grunt karma:dev-form-angularjs
```


#### Mocha CLI

```sh
grunt mochacli
# or
grunt watch:mochacli
```

### Issues

https://app.camunda.com/jira/browse/CAM/component/12351

## Releasing

### Release

To create a release:

```sh
grunt publish:release --setversion='myReleaseVersion'
```

This will update the version, commit and tag it, then publish it to [bower-camunda-bpm-sdk-js](https://github.com/camunda/bower-camunda-bpm-sdk-js)

### Snapshot

To create a snapshot release which just builds current head and publishes it to [bower-camunda-bpm-sdk-js](https://github.com/camunda/bower-camunda-bpm-sdk-js) on a branch named the current version:

```sh
grunt publish:snapshot
```

### Version

If you just want to update the current version:

```sh
grunt publish:version --setversion='myNewVersion'
```

### Available options

* --no-bower -> skip bower release
* --no-write -> dryRun mode

### Examples

* [standalone usage](https://github.com/camunda/camunda-bpm-examples/tree/master/sdk-js)

### Contributing

You are __more than welcome__ to take part on the development of this project!

#### Coding

Clone the repository, add, fix or improve and send us a pull request.
But please take care about the commit messages, [our conventions can be found
here](https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md).

#### Coding style guide

In place of a guide, just follow the formatting of existing code :-)

## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).
