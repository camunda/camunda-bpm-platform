REST API - OpenAPI documentation generation
========

This project generates a single openapi.json containing the OpeanAPI documentation of the Engine Rest API.
Aligned with OpeanAPI specification version [3.0.2](https://github.com/OAI/OpenAPI-Specification/blob/3.0.2/versions/3.0.2.md).

# Table of Contents
1. [Build and phases](#build-and-phases)
2. [Contribution](#contribution)
2.1. [main.ftl](#mainftl)
2.2. [macros](#macros)
2.3. [models](#models)
2.4. [paths](#paths)
2.5. [commons](#commons)
2.5.1. [sorting](#sorting)
2.5.2. [pagination](#pagination)
2.5.3. [more](#more)
2.6. [TODOs](#todos)


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
 | +--macros.ftl
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
 | | +--all.ftl
 | | +--create
 | | | +--post.ftl
 | +--process-instance
 | | +--all.ftl
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
* wrap up of paths and components

By parsing this file end .json file is generated.

### macros

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
* use the macros from the previous section when possible
* for the `property` macros DO NOT forget to put `last = true` param for the last property, that will take care for the comas in the json file
* the DTOs that have sorting or pagination properties should use the [common templates](#commons).

### paths

Contains the endpoints definitions of the Rest API. Instructions:
* each resource has its own folder under /paths (e.g. `/process-instance`, `/deployment`)
* each resource directory contains `all.ftl` file that contains endpoint paths, methods and includes endpoint definition templates.
* the path of the endpoint is resolved to a folder structure (e.g. get process instance count `GET /process-instance/count` goes to `/paths/process-instance/count`).
* the dynamic endpoints should be structured with brakes like `process-instance/{id}/variables/{varName}/data`,
then the path parameters (`id` and `varName`) should always be included in the endpoint definition and marked as `required`.
* the name of the method's request (GET, POST, PUT, DELETE, OPTIONS) is the name of the template file (get.ftl, post.frl, etc.).
* each endpoint definition has a unique `operationId` that will be used for the generation of clients.
In most of the cases, the java method name should be used (e.g. `deleteProcessInstancesAsync`). When this is not possible, please define it according to the Java conventions.
* each endpoint definition contains a tag of its resource (e.g. `Process instance`, `Deployment`).
* each endpoint definition contains a description.
* each endpoint definition contains at least one HTTP response object defined.
* use the macros from the previous section when possible
* for the `property` and `param` macros DO NOT forget to put last = true param for the last property/parameter, that will take care for the comas in the json file
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

##### more
Sometimes the same bunch of parameters is used in multiple endpoints. In cases like there, feel free to create a template and reuse it.
Example: [process-instance-query-params.ftl](./src/main/templates/lib/commons/process-instance-query-params.ftl) used in `getProcessInstancesCount` and `getProcessInstances`

### TODOs

#### Long descriptions
TODO CAM-11377
#### Dates
TODO CAM-11407

