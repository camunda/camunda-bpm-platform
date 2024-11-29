REST API - OpenAPI documentation generation
========

This project generates a single openapi.json containing the OpeanAPI documentation of the Engine Rest API.
Aligned with OpenAPI specification version [3.0.2](https://github.com/OAI/OpenAPI-Specification/blob/3.0.2/versions/3.0.2.md).

# Table of Contents
1. [Build and phases](#build-and-phases)
2. [Contribution](#contribution)
 + [main.ftl](#mainftl)
 + [utils](#utils)
 + [models](#models)
 + [paths](#paths)
 + [commons](#commons)
 + [sorting](#sorting)
 + [pagination](#pagination)
 + [reuse](#reuse)
 + [descriptions](#descriptions)
 + [formats](#formats)
 + [nullable](#nullable)
 + [examples](#examples)
3. [Reviews](#reviews)


## Build and phases

To build the artifact run: `mvn clean install`

The build contains:
* Generation of a formatted openapi.json file by parsing the [templates](./src/main/templates)
* Validation of the generated file against [schema.json](./src/main/openapi/schema.json)
* Generation of a java client from the openapi.json file
* Run of smoke tests against that client (the tests use [WireMock](http://wiremock.org/docs/))

## Contribution

NOTE: Please follow the next step to get familiar with the structure and the instructions that follow.

For the generation of the documentation, we use the [Freemarker](https://freemarker.apache.org/docs/index.html) templating language.
The structure of the template is:

```
 +--main.ftl
 +--lib
 | +--utils.ftl
 | +--commons
 | | +--pagination-params.ftl
 | | +--process-instance-query-params.ftl
 | | +--sort-params.ftl
 | | +--sort-props.ftl
 +--models
 | +--org/camunda/bpm/engine/rest/dto
 | | +--ExceptionDto.ftl
 | | +--history
 | | | +--HistoricProcessInstanceQueryDto.ftl
 | | +--repository
 | | | +--DeploymentDto.ftl
 | | | +--ProcessDefinitionDto.ftl
 | | +--runtime
 | | | +--ActivityInstanceDto.ftl
 +--paths
 | +--deployment
 | | +--create
 | | | +--post.ftl
 | +--process-instance
 | | +--get.ftl
 | | +--suspended
 | | | +--put.ftl
 | | +--{id}
 | | | +--delete.ftl
 | | | +--variables
 | | | | +--{varName}
 | | | | | +--data
 | | | | | | +--get.ftl
 | | | | | | +--post.ftl
 | | | | | +--delete.ftl
```

### main.ftl

The `main.ftl` contains the general information of the OpenAPI doc:
* OpenAPI spec version
* info
* externalDocs
* servers (default and named)
* tags (each resource has a tag)
  * Sub-resources should have the parent tag as prefix (e.g. `Task` and `Task Attachment`)
* wrap up of paths and components

By parsing this file end .json file is generated.

### utils

Contains all of the [macros](https://freemarker.apache.org/docs/ref_directive_macro.html) used in paths and models:
* `parameter` (used in paths)
* `property` (used in paths and models)
* `requestBody` (used in paths)
* `response` (used in paths)

Try to get familiar with them as they are heavily used in the templates.
Keep in mind that in some situations, it is fine not to use them, if the endpoint/dto is too complex, for example.
Feel free to add missing parameters to the macros, however, do not forget to reflect your changes to all usages of the macros.
Some parameters are required (`name` and `description` in most of the cases),
and some need to be provided if necessary (nice to have - `minimum`,`defaultValue`, `deprecated`). 

### models

This folder contains all of the DTOs used in the request and response bodies. Instructions:
* use the name and package structure of the Rest DTOs when possible
([org.camunda.bpm.engine.rest.dto.ExceptionDto.java](https://github.com/camunda/camunda-bpm-platform/blob/master/engine-rest/engine-rest/src/main/java/org/camunda/bpm/engine/rest/dto/ExceptionDto.java) --> 
[org/camunda/bpm/engine/rest/dto/ExceptionDto.ftl](https://github.com/camunda/camunda-bpm-platform/blob/master/engine-rest/engine-rest-openapi/src/main/templates/models/org/camunda/bpm/engine/rest/dto/ExceptionDto.ftl))
Keep the properties of OpenAPI doc as close as possible to the Java DTOs and add explicit description whenever a property is not applicable to a certain endpoint (e.g. [PUT /process-instance/suspended](https://github.com/camunda/camunda-bpm-platform/blob/master/engine-rest/engine-rest-openapi/src/main/templates/paths/process-instance/suspended/put.ftl))
* the definitions of the models are resolved automatically via the folder structure. The `/models` directory should contain only the models that are used in the documentation, any additional files (macros and reusable files) should go to [commons](#commons), do not create empty folders. The models are ordered lexicographically.
* use the [utils](#utils) from the previous section when possible.
* use the `dto` macro to define a DTO
  * in case of a DTO hierarchy (`TriggerVariableValueDto extends VariableValueDto`), the `dto` macro provides an `extends` 
  attribute that makes use of the `allOf` OpenAPI syntax - [example](https://github.com/camunda/camunda-bpm-platform/blob/392d98b61e5e0eff3e1dad0ee15a5ad986e0d93c/engine-rest/engine-rest-openapi/src/main/templates/models/org/camunda/bpm/engine/rest/dto/runtime/TriggerVariableValueDto.ftl#L2-L19).
  * the `property` macros should be nested inside the `dto` macro
* in case the response can be two DTOs depending on request parameter (example - [message correlation](https://docs.camunda.org/manual/develop/reference/rest/message/post-message/#result) and responses `MessageCorrelationResultDto` or `MessageCorrelationResultWithVariableDto` (extending `MessageCorrelationResultDto`)), please use the DTO for the response that contains all of the properties (in the correlation case - `MessageCorrelationResultWithVariableDto`) even some are not applicable for all of the responses, and make sure to document which properties are not applicable in which use cases. (In some use cases `oneOf` approach might be applicable together with `discrimitator` ([spec](https://github.com/OAI/OpenAPI-Specification/blob/3.0.2/versions/3.0.2.md#discriminator-object)), please test this additionally as the clients might have problems to be generated in this approach.)
* for the `property` macros DO NOT forget to put `last = true` param for the last property, that will take care for the commas in the json file.
* the DTOs that have sorting or pagination properties should use the [common templates](#commons).

### paths

Contains the endpoints definitions of the Rest API. Instructions:
* each resource has its own folder under /paths (e.g. `/process-instance`, `/deployment`)
* the path of the endpoint is resolved to a folder structure (e.g. get process instance count `GET /process-instance/count` goes to `/paths/process-instance/count`).
NOTE: The endpoints' paths are automatically resolved from the folder structure, please keep the file structure of `/paths` clean, without any additional files, different than the endpoint definitions (for example the reusable templates should go in [commons](#commons), do not create empty folders and so on).
The endpoints' paths are ordered lexicographically. 
* the dynamic endpoints should be structured with brakes like `process-instance/{id}/variables/{varName}/data`,
then the path parameters (`id` and `varName`) should always be included in the endpoint definition and marked as `required`.
* endpoints that are almost similar but have a different paths (e.g. [Get Activity Instance Statistics](https://docs.camunda.org/manual/7.12/reference/rest/process-definition/get-activity-statistics/)) needs to be separated in different files. A unique `operationId` should be assigned to each of them. You can consider adding the common parts of the endpoints in [lib/commons](#commons).
* the name of the method's request (GET, POST, PUT, DELETE, OPTIONS) is the name of the template file (get.ftl, post.frl, etc.).
* each endpoint definition has a unique `operationId` that will be used for the generation of clients.
* for `async` endpoints make sure to add `Operation` suffix to prevent collisions in generated C# clients, e.g. `setExternalTaskRetriesAsync` -> `setExternalTaskRetriesAsyncOperation`, `modifyProcessInstanceAsync` -> `modifyProcessInstanceAsyncOperation`
In most of the cases, the java method name should be used (e.g. `deleteProcessInstancesAsync`). When this is not possible, please define it according to the Java conventions.
* each endpoint definition contains a tag of its resource (e.g. `Process instance`, `Deployment`).
* each endpoint definition contains a description and summary (the latter is the title of Rest API doc page).
* each endpoint definition contains at least one HTTP response object defined.
* in the request body try to use a DTO when possible, always check the Java DTO for guidance;
avoid constructing a request body without DTO and only with properties defined in the endpoint description.
* use the [utils](#utils) from the previous section when possible
* for the `property` and `param` macros DO NOT forget to put last = true param for the last property/parameter, that will take care for the commas in the json file
* the endpoints that have sorting or pagination properties/parameter should use the [common templates](#commons).

#### commons

Contains common templates that can be reused when it's possible.

##### sorting

* [sort-params.ftl](./src/main/templates/lib/commons/sort-params.ftl)
* [sort-props.ftl](./src/main/templates/lib/commons/sort-props.ftl)

Please set the `sortByValues` enumeration whenever the template is in use and do not forget to assign the `last = true` if this is the last parameter/property (taking care for the commas in the json):
```
<#-- <#assign last = true >  --> <#-- remove comment if last param  -->
<#assign sortByValues = ['"instanceId"', '"definitionId"', '"definitionKey"', '"definitionName"', '"definitionVersion"', '"businessKey"', '"startTime"', '"endTime"', '"duration"', '"tenantId"']>
<#include "/lib/commons/sort-props.ftl" >
```

##### pagination

* [pagination-params.ftl](./src/main/templates/lib/commons/pagination-params.ftl)

Use whenever `firstResult` and `maxResults` are part of the endpoint parameters. Do not forget to assign `last=true` param in case those are the last parameters:
```
    <#-- <#assign last = true >  --> <#-- remove comment if last param  -->
    <#include "/lib/commons/pagination-params.ftl" >
```

##### reuse
Sometimes the same bunch of parameters is used in multiple endpoints. In cases like there, feel free to create a template and reuse it.
Example: [process-instance-query-params.ftl](./src/main/templates/lib/commons/process-instance-query-params.ftl) used in `getProcessInstancesCount` and `getProcessInstances`

### Parameters and properties

#### Descriptions
Markdown can be used for text descriptions: 

    `monospace` (code)
    *italic* or _italic_ (cursive)
    **bold** or __bold__
    * bullet point

Recommendations:
* use unix line endings
* to add links use markdown, e.g. `[link](http://example.com)`
* Add `docsUrl` to resolve doc link - [User guide](${docsUrl}/user-guide/process-engine/process-instance-modification/)
`docsUrl="https://docs.camunda.org/manual/${docsVersion}"
* keep line length to a maximum of 120 characters
* use indentation, avoid adding long descriptions on a single line,
improve the readibility by splitting the next with single or multiple line breaks:

```
  "description": "Submits a list of modification instructions to change a process instance's execution state.
                  A modification instruction is one of the following:

                  * Starting execution before an activity
                  * Starting execution after an activity on its single outgoing sequence flow
                  * Starting execution on a specific sequence flow
                  * Canceling an activity instance, transition instance, or all instances (activity or transition) for an activity

                  Instructions are executed immediately and in the order they are provided in this request's body.
                  Variables can be provided with every starting instruction.

                  The exact semantics of modification can be read about in the [User guide](https://docs.camunda.org/manual/develop/user-guide/process-engine/process-instance-modification/)."
```

#### Formats

The `format` fields further define the type of properties and parameters. For more information, please check the [OpenAPI spec](https://github.com/OAI/OpenAPI-Specification/blob/3.0.2/versions/3.0.2.md#dataTypeFormat). The common types used in the documentation are `int32`, `binary`, `date-time`.
Example:
Specify the `date-time` format of the date properties whenever is possible,
```
    <@lib.property
        name = "startedBefore"
        type = "string"
        format = "date-time"
        description = "Restrict to instances that were started before the given date.
                       By default (https://docs.camunda.org/manual/${docsVersion}/reference/rest/overview/date-format/),
                       the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., 2013-01-23T14:42:45.000+0200." />
```
That will improve the clients that are generated from the OpenAPI documentation. **Note:** When using a 'date-time' in a 'GET' request (as URL parameter), it is important to use proper URL encoding as described in the [Date Format Overview](https://docs.camunda.org/manual/develop/reference/rest/overview/date-format/)

#### Nullable

By default, the properties of type `boolean`, `string`, `array`, and `dto` have `nullable` field enabled by default to ensure that the OpenAPI specification can work with C# clients.
That way we create relaxed specification for clients where the fields should be specified explicitly whether they should be null or not.
Disable `nullable` field to the properties where it's sure that they are required:
```
    <@lib.property
         name = "version"
         type = "integer"
         format = "int32"
         nullable = false
         desc = "The version of the process definition that the engine assigned to it." />
```

#### Examples

You can add an examples to `requestBody` or `response` as follows:
* pass the example inside single quotes instead of quotes as the examples contain quotes and they should be escaped
* multiple examples are allowed
  * each example should be a separate JSON object in the `examples` array (e.g. `example-1`, `example-2`)
* each example should have a unique name
* add a `description` and/or `summary` properties when necessary
* the `value` property is required and should contain the example content
```
  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetJobRetriesByProcessDto"
      requestDesc = "Please note that if both processInstances and processInstanceQuery are provided,
                     then the resulting execution will be performed on the union of these sets.
                     **Unallowed property**: `historicProcessInstanceQuery`"
      examples = ['"example-1": {
                       "value": {
                         "retries": numberOfRetries,
                         "processInstances": ["aProcess", "secondProcess"],
                         "processInstanceQuery": {
                           "processDefinitionId": "aProcessDefinitionId"
                         }
                       }
                    }']/>
```

## Reviews

How to do reviews?

* in case of changes of the macros, please compare the last build openapi.json version with one to review to ensure that the endpoins are complete after the new changes
* double check the path of the endpoints as they are not easy to see if they are not checked additionally
* check for correct parameters, requests, and responses
* check for compliance with the Java endpoints (`endpointInfo` id should be as close as possible to the Java method in the DTO packages - check the `\*RestService.java` and `\*Resource.java` interfaces)
* you can compare the generated client (from the tests) with the actual REST API - most of the case they should look similar
