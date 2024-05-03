# Handler Function

The handler function is used to handle a fetched task.

```js
const { Client, logger, Variables } = require("camunda-external-task-client-js");

// create a Client instance with custom configuration
const client = new Client({ baseUrl: "http://localhost:8080/engine-rest", use: logger });

// create a handler function
const handler = async function({ task, taskService }) {
  // get the process variable 'score'
  const score = Variables.get("score");
  // set a process variable 'winning'
  const processVariables = new Variables().set("winning", score > 5);
  // set a local variable 'winningDate'
  const localVariables = new Variables().set("winningDate", new Date());

  // complete the task
  await taskService.complete(task, processVariables, localVariables);
};

// susbscribe to the topic: 'topicName'
client.subscribe("topicName", handler);
```

## `task.variables`

The `variables` object is a read-only [Variables](/docs/Variables) instance.  
It provides various [getters](/docs/Variables#variablesgetvariablename)
for reading the process variables in the scope of the service task.

```js
client.subscribe("bar", async function({ task, taskService }) {
  // output all process variables
  console.log(task.variables.getAll());
});
```
## `taskService.complete(task, processVariables, localVariables)`

| Parameter        | Description                                  | Type             | Required |
|------------------|----------------------------------------------|------------------|----------|
| task             | task or id of the task to complete           | object or string | ✓        |
| processVariables | map of variables to set in the process scope | object           |          |
| localVariables   | map of variables to set in the task scope    | object           |          |

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic

  // Complete the task
  await taskService.complete(task);
});
```

## `taskService.handleFailure(task, options)`

| Parameter | Description                              | Type             | Required |
|-----------|------------------------------------------|------------------|----------|
| task      | task or id of the task to handle failure. | object or string | ✓        |
| options   | options about the failure.                | object           |          |


options include:

| Option       | Description                                                                                                                                                                                                                                      | Type   | Required | Default |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|----------|---------|
| errorMessage | An message indicating the reason of the failure.                                                                                                                                                                                                 | string |          |         |
| errorDetails | A detailed error description.                                                                                                                                                                                                                    | string |          |         |
| retries      | A number of how often the task should be retried. Must be >= 0. If this is 0, an incident is created and the task cannot be fetched anymore unless the retries are increased again. The incident's message is set to the errorMessage parameter. | number |          |         |
| retryTimeout | A timeout in milliseconds before the external task becomes available again for fetching. Must be >= 0.                                                                                                                                           | number |          |         |

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

## `taskService.handleBpmnError(task, errorCode)`
| Parameter    | Description                                                                                    | Type             | Required |
|--------------|------------------------------------------------------------------------------------------------|------------------|----------|
| task         | task or id of the task to handle bpmn failure                                                  | object or string | ✓        |
| errorCode    | An error code that indicates the predefined error. Is used to identify the BPMN error handler. | string           | ✓        |
| errorMessage | An error message that describes the error.                                                     | string           |          |
| variables    | Map of variables which will be passed to the execution.                                        |                  |          |

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

## `taskService.extendLock(task, newDuration)`

| Parameter   | Description                                                                                          | Type             | Required |
|-------------|------------------------------------------------------------------------------------------------------|------------------|----------|
| task        | task or id of the task to extend lock duration                                                       | object or string | ✓        |
| newDuration | An amount of time (in milliseconds). This is the new lock duration starting from the current moment. | number           | ✓        |

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic

  // Extend the lock time
  try {
    await taskService.extendLock(task, 5000);
    console.log("I extended the lock time successfully!!");
  } catch (e) {
    console.error(`Failed to extend the lock time, ${e}`);
  }
});
```

## `taskService.unlock(task)`

| Parameter | Description                      | Type             | Required |
|-----------|----------------------------------|------------------|----------|
| task      | task or id of the task to unlock | object or string | ✓        |

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Put your business logic

  // Unlock the task
  await taskService.unlock(task);
});
```

## `taskService.lock(task, newDuration)`

| Parameter   | Description                                                                                      | Type             | Required |
|-------------|--------------------------------------------------------------------------------------------------|------------------|----------|
| task        | task or id of the task to extend lock duration                                                   | object or string | ✓        |
| duration    | An amount of time (in milliseconds). This is the lock duration starting from the current moment. | number           | ✓        |

```js
// Susbscribe to the topic: 'topicName'
client.subscribe("topicName", async function({ task, taskService }) {
  // Task is locked by default
  // Put your business logic, unlock the task or let the lock expire

  // Lock a task again
  try {
    await taskService.lock(task, 5000);
    console.log("I locked the task successfully!");
  } catch (e) {
    console.error(`Failed to lock the task, ${e}`);
  }
});
```