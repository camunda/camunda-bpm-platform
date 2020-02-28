{
  <@lib.endpointInfo
      id = "getProcessInstancesCount"
      tag = "Process instance"
      desc = "Queries for the number of process instances that fulfill given parameters." />

  "parameters": [
    <#assign last = true >
    <#include "/lib/commons/process-instance-query-params.ftl">
  ],
  "responses": {
    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example an invalid operator for variable comparison is used."/>
  }
}