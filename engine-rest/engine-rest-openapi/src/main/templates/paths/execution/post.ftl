<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-query/index.html -->
{
  <@lib.endpointInfo
      id = "queryExecutions"
      tag = "Execution"
      summary = "Get Executions (POST)"
      desc = "Queries for executions that fulfill given parameters through a JSON object.
              This method is slightly more powerful than the [Get
              Executions](${docsUrl}/reference/rest/execution/get-query/) method
              because it allows
              to filter by multiple instance and execution variables of types
              `String`, `Number` or `Boolean`."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExecutionQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/execution`",
                     "value": {
                       "variables": [
                         {
                           "name": "myVariable",
                           "operator": "eq",
                           "value": "camunda"
                         },
                         {
                           "name": "mySecondVariable",
                           "operator": "neq",
                           "value": 124
                         }
                       ],
                       "processDefinitionId": "aProcessDefinitionId",
                       "sorting": [
                         {
                           "sortBy": "definitionKey",
                           "sortOrder": "asc"
                         },
                         {
                           "sortBy": "instanceId",
                           "sortOrder": "desc"
                         }
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "ExecutionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/execution`",
                       "value": [
                         {
                           "id": "anId",
                           "processInstanceId": "aProcInstId",
                           "ended": false,
                           "tenantId": null
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`, or if an invalid operator
                for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>