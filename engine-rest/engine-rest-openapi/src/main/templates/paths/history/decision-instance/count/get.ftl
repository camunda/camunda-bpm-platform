<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-instance/get-decision-instance-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricDecisionInstancesCount"
      tag = "Historic Decision Instance"
      summary = "Get Historic Decision Instance Count"
      desc = "Queries for the number of historic decision instances that fulfill the given parameters. 
              Takes the same parameters as the 
              [Get Historic Decision Instances](${docsUrl}/reference/rest/history/decision-instance/get-decision-instance-query/) 
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/historic-decision-instance-query-params.ftl" >
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
                       "summary": "GET `/history/decision-instance/count`",
                       "value": {
                         "count": 4
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
