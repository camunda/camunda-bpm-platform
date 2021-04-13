<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/get-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "getExecutionsCount"
      tag = "Execution"
      summary = "Get Execution Count"
      desc = "Queries for the number of executions that fulfill given parameters.
              Takes the same parameters as the [Get
              Executions](${docsUrl}/reference/rest/execution/get-query/) method."
  />

  "parameters" : [

    <#assign last = true >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/execution-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/execution/count?variables=myVariable_eq_camunda`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if an invalid operator
                for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>