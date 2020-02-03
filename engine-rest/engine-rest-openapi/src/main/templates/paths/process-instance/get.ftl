{
  "operationId" : "getProcessInstances",
  "description": "Queries for process instances that fulfill given parameters. Parameters may be static as well as dynamic runtime properties of process instances. The size of the result set can be retrieved by using the Get Instance Count method.",
  "tags": [
    "Process instance"
  ],
  "parameters" : [ 

    <#assign sortByValues = ['"instanceId"', '"definitionKey"', '"definitionId"', '"tenantId"', '"businessKey"']>
    <#include "/paths/commons/sort-params.ftl" >

    <#include "/paths/commons/pagination-params.ftl" >

    <#assign last = true >
    <#include "/paths/commons/process-instance-query-params.ftl" >

  ],
  "responses" : {
    <@lib.response
        code="200"
        dto="ProcessInstanceDto"
        array=true
        desc="Request successful." />

    <@lib.response
        code="400"
        dto="ExceptionDto"
        last =true
        desc="Bad Request\n\nReturned if some of the query parameters are invalid, for example if a sortOrder parameter is supplied, but no sortBy, or if an invalid operator for variable comparison is used."/>
  }
}