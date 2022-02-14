<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/get-query/index.html -->
{
  <@lib.endpointInfo
      id = "getExecutions"
      tag = "Execution"
      summary = "Get Executions"
      desc = "Queries for the executions that fulfill given parameters.
              Parameters may be static as well as dynamic runtime properties of
              executions.
              The size of the result set can be retrieved by using the [Get
              Execution Count](${docsUrl}/reference/rest/execution/get-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/execution-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "ExecutionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/execution?variables=myVariable_eq_camunda`",
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