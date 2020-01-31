{
  "operationId": "getProcessInstancesCount",
  "description": "Queries for the number of process instances that fulfill given parameters.",
  "tags": [
    "Process instance"
  ],
  "parameters": [
    <#include "/paths/commons/process-instance-query-params.ftl">
  ],
  "responses": {
    <@lib.response
        code="200"
        dto="CountResultDto"
        desc="Request successful."/>

    <@lib.response
        code="400"
        dto="ExceptionDto"
        last =true
        desc="Bad Request
Returned if some of the query parameters are invalid, for example an invalid operator for variable comparison is used."/>
  }
}