<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/get-variable-instance-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricVariableInstancesCount"
      tag = "Historic Variable Instance"
      summary = "Get Variable Instance Count"
      desc = "Queries for the number of historic variable instances that fulfill the given
              parameters.
              Takes the same parameters as the
              [Get Variable Instances](${docsUrl}/reference/rest/history/variable-instance/get-variable-instance-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/historic-variable-instance-query-params.ftl" >
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
                       "summary": "GET `/history/variable-instance/count?variableName=my_variable`",
                       "description": "GET `/history/variable-instance/count?variableName=my_variable`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
