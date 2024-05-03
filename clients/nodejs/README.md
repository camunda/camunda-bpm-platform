# camunda-external-task-client

[![npm version](https://badge.fury.io/js/camunda-external-task-client-js.svg)](https://badge.fury.io/js/camunda-external-task-client-js)
![CI](https://github.com/camunda/camunda-external-task-client-js/actions/workflows/CI.yml/badge.svg)

Implement your [BPMN Service Task](https://docs.camunda.org/manual/latest/user-guide/process-engine/external-tasks/) in
NodeJS.

> This package is an [ECMAScript module](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Modules) (ESM) and provides no CommonJS exports.

> NodeJS >= v18 is required

## Installing

```sh
npm install -s camunda-external-task-client-js
```

Or:

```sh
yarn add camunda-external-task-client-js
```

## Usage

1.  Make sure to have [Camunda](https://camunda.com/download/) running.
2.  Create a simple process model with an External Service Task and define the topic as 'topicName'.
3.  Deploy the process to the Camunda Platform engine.
4.  In your NodeJS script:

```js
import { Client, logger } from "camunda-external-task-client-js";

// configuration for the Client:
//  - 'baseUrl': url to the Process Engine
//  - 'logger': utility to automatically log important events
const config = { baseUrl: "http://localhost:8080/engine-rest", use: logger };

// create a Client instance with custom configuration
const client = new Client(config);

// susbscribe to the topic: 'creditScoreChecker'
client.subscribe("creditScoreChecker", async function({ task, taskService }) {
  // Put your business logic
  // complete the task
  await taskService.complete(task);
});
```

> **Note:** Although the examples used in this documentation use _async await_ for handling asynchronous calls, you
> can also use Promises to achieve the same results.

## About External Tasks

External Tasks are service tasks whose execution differs particularly from the execution of other service tasks (e.g. Human Tasks).
The execution works in a way that units of work are polled from the engine before being completed.

**camunda-external-task-client.js** allows you to create easily such client in NodeJS.

## Features

### [Fetch and Lock](https://docs.camunda.org/manual/latest/reference/rest/external-task/fetch/)

Done through [polling](/docs/Client.md#about-polling).

### [Complete](https://docs.camunda.org/manual/latest/reference/rest/external-task/post-complete/)

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic
  // Complete the task
  await taskService.complete(task);
});
```

### [Handle Failure](https://docs.camunda.org/manual/latest/reference/rest/external-task/post-failure/)

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic
  // Handle a Failure
  await taskService.handleFailure(task, {
    errorMessage: "some failure message",
    errorDetails: "some details",
    retries: 1,
    retryTimeout: 1000
  });

});
```

### [Handle BPMN Error](https://docs.camunda.org/manual/latest/reference/rest/external-task/post-bpmn-error/)

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic

  // Create some variables
  const variables = new Variables().set('date', new Date());

  // Handle a BPMN Failure
  await taskService.handleBpmnError(task, "BPMNError_Code", "Error message", variables);
});
```

### [Extend Lock](https://docs.camunda.org/manual/latest/reference/rest/external-task/post-extend-lock/)

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic
  // Extend the lock time
  await taskService.extendLock(task, 5000);
});
```

### [Unlock](https://docs.camunda.org/manual/latest/reference/rest/external-task/post-unlock/)

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic
  // Unlock the task
  await taskService.unlock(task);
});
```

### [Lock](https://docs.camunda.org/manual/latest/reference/rest/external-task/post-lock/)
```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Task is locked by default
  // Put your business logic, unlock the task or let the lock expire

  // Lock a task again
  await taskService.lock(task, 5000);
});
```

### Exchange Process & Local Task Variables

```js
import { Variables } from "camunda-external-task-client-js";

client.subscribe("topicName", async function({ task, taskService }) {
  // get the process variable 'score'
  const score = task.variables.get("score");

  // set a process variable 'winning'
  const processVariables = new Variables();
  processVariables.set("winning", score > 5);

  // set a local variable 'winningDate'
  const localVariables = new Variables();
  localVariables.set("winningDate", new Date());

  // complete the task
  await taskService.complete(task, processVariables, localVariables);
});
```

## API

* [Client](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Client.md)
  * [new Client(options)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Client.md#new-clientoptions)
  * [client.start()](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Client.md#clientstart)
  * [client.subscribe(topic, [options], handler)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Client.md#clientsubscribetopic-options-handler)
  * [client.stop()](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Client.md#clientstop)
  * [Client Events)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Client.md#client-events)
  * [About the Handler Function](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/handler.md)
* [Variables](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md)
  * [new Variables(options)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#new-variablesoptions")
  * [variables.get(variableName)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablesgetvariablename)
  * [variables.getTyped(variableName)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablesgettypedvariablename)
  * [variables.getAll()](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablesgetall)
  * [variables.getAllTyped()](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablesgetalltyped)
  * [variables.set(variableName)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablessetvariablename-value)
  * [variables.setTyped(variableName)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablessettypedvariablename-typedvalue)
  * [variables.setAll(values)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablessetallvalues)
  * [variables.setAllTyped(typedValues)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#variablessetalltypedtypedvalues)
  * [About JSON & Date Variables](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/Variables.md#about-json--date-variables)
* [File](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/File.md)
  * [new File(options)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/File.md#new-fileoptions)
  * [file.load()](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/File.md#fileload)
  * [File Properties](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/File.md#file-properties)
* [BasicAuthInterceptor](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/BasicAuthInterceptor.md)
  * [new BasicAuthInterceptor(options)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/BasicAuthInterceptor.md#new-basicauthinterceptoroptions)
* [KeycloakAuthInterceptor](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/KeycloakAuthInterceptor.md)
  * [new KeycloakAuthInterceptor(options)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/KeycloakAuthInterceptor.md#new-keycloakauthinterceptoroptions)
* [logger](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/logger.md)
  * [logger.success(text)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/logger.md#loggersuccesstext)
  * [logger.error(text)](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/logger.md#loggererrortext)

## Contributing

Have a look at our [contribution guide](https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md) for how to contribute to this repository.

## Help and support

* [Documentation](https://docs.camunda.org/manual/latest/)
* [Forum](https://forum.camunda.org)
* [Stackoverflow](https://stackoverflow.com/questions/tagged/camunda)

## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).
