<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricDetailsCount"
      tag = "Historic Detail"
      summary = "Get Historic Detail Count"
      desc = "Queries for the number of historic details that fulfill the given parameters.
              Takes the same parameters as the [Get Historic
              Details](${docsUrl}/reference/rest/history/detail/get-detail-query/)
              method."
  />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-detail-query-params.ftl" >
    <@lib.parameters
        object = params
        last = true
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/detail/count?variableName=my_variable`",
                       "description": "GET `/history/detail/count?variableName=my_variable`",
                       "value": {
                         "count": 3
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid."
        last = true
    />

  }

}
</#macro>