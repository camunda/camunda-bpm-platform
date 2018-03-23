# Camunda External Task Client (Java)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-external-task-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-external-task-client)

The **Camunda External Task Client (Java)** allows to set up remote Service Tasks for your workflow.

> **Heads Up!** 
>
> This project is under heavy development and is not meant to be used as part of production environments.

## Features
* Complete External Tasks
* Extend the lock duration of External Tasks
* Unlock External Tasks
* Report BPMN errors as well as failures
* Share primitive and object typed process variables with the Workflow Engine

## Documentation
### Table of Contents
1. [Prerequisites](#prerequisites)
2. [Maven coordinates](#maven-coordinates)
3. [Bootstrapping the Client](#bootstrapping-the-client)
    * [Request Interceptors](#request-interceptors)
      * [Custom Request Interceptors](#custom-request-interceptors)
      * [Basic Authentication](#basic-authentication)
5. [Topic Subscription](#topic-subscription)
6. [Handler](#handler)
7. [Completing Tasks](#completing-tasks)
8. [Extending the Lock Duration of Tasks](#extending-the-lock-duration-of-tasks)
9. [Unlocking Tasks](#unlocking-tasks)
10. [Reporting Failures](#reporting-failures)
11. [Reporting BPMN Errors](#reporting-bpmn-errors)
12. [Process Variables](#process-variables)
    * [Supported Types](#supported-types)
    * [Untyped Variables](#untyped-variables)
    * [Typed Variables](#typed-variables)
    * [Object Typed Variables](#object-typed-variables)
    * [Transient Variables](#transient-variables)
13. [Logging](#logging)
14. [License](#license)

### Prerequisites
* Oracle Hotspot v1.8+ (JDK 8)

### Maven coordinates
The following Maven coordinates need to be added to the projects `pom.xml`:
```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-external-task-client</artifactId>
  <version>${version}</version>
</dependency>
```

### Bootstrapping the Client
To configure and instantiate the client, a convenient fluent builder can be used. Calling `ExternalTaskClient#create`
starts the configuration chain:

```java
ExternalTaskClient client = ExternalTaskClient.create()
  .baseUrl("http://localhost:8080/engine-rest")
  .build();
```

Since a HTTP based communication between the client and the Camunda Workflow Engine is used, the base url needs to point 
to the respective URL of the REST API. **Base url** is a mandatory information.

#### Request Interceptors
To add additional HTTP headers to the performed REST API requests, the concept of request interceptors comes in handy. 
This concept is, for instance, necessary in context of authentication. Request interceptors can be added while bootstrapping 
the client.

##### Custom Request Interceptors
For the purpose of adding custom request interceptors, the `ClientRequestInterceptor` can be implemented. Consider the 
following code snippet which shows the method stub that can be implemented:

```java
public class CustomRequestInterceptor implements ClientRequestInterceptor {

  public void intercept(ClientRequestContext requestContext) {
    // custom implementation
  }

}
```

The method `CustomRequestInterceptor#intercept` provides access to `ClientRequestContext#addHeader` which allows to add 
headers to the HTTP requests.

Once implemented, a custom request interceptor can be added by calling `#addInterceptor` while bootstrapping the client:

```java
...
.addInterceptor(new CustomRequestInterceptor())
...
```

##### Basic Authentication
In some cases it is necessary to secure the REST API of the Camunda Workflow Engine via Basic Authentication. For such
situations the a Basic Authentication implementation is provided by the client. On instantiation of `BasicAuthProvider` 
the respective credentials are demanded. This instance needs to be added to the client as a request interceptor. Once added, 
the basic authentication header based on the provided credentials is added to each REST API request.

```java
...
.addInterceptor(new BasicAuthProvider("username", "password"))
...
```

### Topic Subscription
If a Service Task of the Type "External" is placed inside a workflow, a topic name must be specified. The corresponding
BPMN 2.0 XML could look as follows:

```xml
...
<serviceTask id="checkCreditScoreTask"
  name="Check credit score"
  camunda:type="external"
  camunda:topic="creditScoreChecker" />
...
```
As soon as the Workflow Engine has been executed an External Task, an activity instance is created and waits to be 
fetched and locked by a client.

The client subscribes to the topic and fetches continuously for newly appearing External Tasks provided by the 
Workflow Engine. Each fetched External Task is marked with a temporary lock. Like this, no other clients can work on this 
certain External Task in the meanwhile.

A new topic subscription can be configured by calling `client#subscribe`. The topic name, the lock duration in milliseconds as well as the handler are mandatory information. See the following code snippet:

```java
// ...

client.subscribe("creditScoreChecker")
  .lockDuration(1000)
  .handler((externalTask, externalTaskService) -> {

    // interact with the external task

  }).open();
```

Once a topic has been subscribed, the client starts immediately to fetch and lock External Tasks. A lock is valid for a 
specified period of time – also known as lock duration.
The External Task is now invisible for other clients and cannot be locked. As soon as the lock duration expires, the actions 
provided by the `ExternalTaskService` cannot be applied to the External Task anymore. In this case, the External Task is 
released and available again for being fetched and locked by clients.

### Handler
A handler object has to be provided for each topic subscription. To do so, the lambda expression `(externalTask, externalTaskService) -> {...}`
can be used to implement a custom routine which is invoked whenever an External Task is fetched and locked successfully. 
The lambda expression provides access to the respective `ExternalTask` as well as the `ExternalTaskService`.

### Completing Tasks
Once the work is done, the External Task can be completed. This means for the Workflow Engine that the execution will 
move on. For this purpose, `ExternalTaskService#complete` can be called and the External Task needs to be passed. The 
External Task can only be completed, if it is currently locked by the client. Otherwise a `NotAcquiredException` is thrown.

### Extending the Lock Duration of Tasks
Sometimes the completion of the work lasts longer than expected before. In this case the lock duration needs to be extended. 
This action can be performed by calling `ExternalTaskService#extendLock` and passing the External Task and the new lock duration.
The lock duration can only be extended, if the External Task is currently locked by the client. Otherwise a `NotAcquiredException`
is thrown.

### Unlocking Tasks
If a lock of an External Task should be returned so that other clients are allowed to fetch and lock this task again, 
`ExternalTaskService#unlock` can be called while passing the External Task. The External Task can only be unlocked, 
if the task is currently locked by the client. Otherwise a `NotAcquiredException` is thrown.

### Reporting Failures
If the client faces a problem that makes it impossible to complete the External Task successfully, this problem can be reported to 
the Workflow Engine. The following parameters need to be passed on calling `ExternalTaskService#handleFailure`:

* External Task
* Error message: a short description of the failure (limited to 666 characters)
* Error details: a detailed error message (unlimited size)
* Retries: amount of fetch and lock actions; each successful performed fetch and lock action decrements the counter; 
If zero, an incident is created
* Retry timeout: the time period in milliseconds between two fetch and lock actions

A failure can only be reported, if the External Task is currently locked by the client. Otherwise a `NotAcquiredException` is thrown.

You can find a detailed documentation about this action in the Camunda BPM [User Guide](https://docs.camunda.org/manual/develop/user-guide/process-engine/external-tasks/#reporting-task-failure).

### Reporting BPMN Errors
[Error boundary events](https://docs.camunda.org/manual/develop/reference/bpmn20/events/error-events/#error-boundary-event) 
are triggered by BPMN errors. On calling `ExternalTaskService#handleBpmnError` a BPMN error can be passed.

A BPMN error can only be reported, if the External Task is currently locked by the client. Otherwise a `NotAcquiredException` is thrown.

You can find a detailed documentation about this action in the Camunda BPM [User Guide](https://docs.camunda.org/manual/develop/user-guide/process-engine/external-tasks/#reporting-bpmn-error).

### Process Variables
Information can be shared between the client and the Workflow Engine with the concept of process variables. The client
supports a wide range of primitive types.

#### Supported Types
* Null
* Boolean
* String
* Date
* Short, Integer, Long
* Double
* Bytes

Setting other types besides the listed ones cause an `UnsupportedTypeException`.

There exists two ways to work with variables: using the typed or the untyped API.

#### Untyped Variables
Untyped variables are stored by using the respective type of their values.

```java
externalTask.setVariable("defaultScore", 5);
```

In the example shown above, the variable is stored as an integer due to the implicit type of the value.

Multiple variables can be stored with `ExternalTask#setAllVariables`. A map has to be passed where each key value pair 
represents a variable name and its value.

**Note:** setting variables does not make sure that variables are persisted. Variables which were set locally on client-side 
are only available during runtime and get lost if they are not shared with the Workflow Engine by successfully completing 
the External Task of the current lock.

Besides storing, it is also possible to retrieve a variable:

```java
int defaultScore = (int) externalTask.getVariable("defaultScore");
```

The return value has to be casted to the expected typed.

`ExternalTask#getAllVariables` can be used to retrieve all variables at once.

#### Typed Variables
For typed variables the type is set explicitly. During compile time it is checked whether or not the given value 
corresponds to the defined type. The following example shows how an typed integer variable is set:

```java
externalTask.setVariableTyped("defaultScore", Variables.integerValue(5));
```

Multiple variables can be stored with `ExternalTask#setAllVariablesTyped`. A map has to be passed where each key value 
pair represents a variable name and its typed value.

Typed variables can also be retrieved:

```java
IntegerValue defaultScoreIntegerValue = externalTask.getVariableTyped("defaultScore");
```

The returned object provides information about the type, the value and if the variable is transient or not.

`ExternalTask#getAllVariablesTyped` can be used to retrieve all variables at once.

#### Object Typed Variables
To store an object with an arbitrary type, it must be serialized as follows:

```java
List<Integer> creditScores = new ArrayList<>(Arrays.asList(9, 1, 4, 10));

ObjectValue creditScoresObject = Variables
  .objectValue(creditScores)
  .create();
```

The serialized object can than be passed as a typed variable value:

```java
externalTask.setVariableTyped("creditScores", creditScoresObject);
```

**Note:** to make sure that an object is readable by other clients or by the Workflow Engine, the respective class must 
exist on the respective class path.

The client supports the serialization format JSON by default. The serialization formats XML and Java are currently not
supported.

#### Transient Variables
Transient variables are not persisted. They only exist during the current transaction. Whenever a process instance reaches
a waiting state, transient variables get lost. Transient variables can only be defined via the typed value API:

```java
StringValue stringValue = Variables.stringValue("transientVariable", true);
```
### Logging
The client uses SLF4J for logging. Since handlers are not invoked in the main thread it is sensible to enable logging 
and be reported about the following situations:

* External Tasks could not be fetched and locked successfully
* An exception occurred...
   * while invoking a handler
   * while deserializing variables
   * while invoking a request interceptor
 
 Any implementations that rely on SLF4J can be used. Logging can be enabled by simply add the desired implementation as 
 a dependency to the `pom.xml`:
 
 ```xml
 <dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.6.1</version>
 </dependency>
 ```

## License
Unless otherwise specified this project is licensed under [Apache License Version 2.0](./LICENSE).
