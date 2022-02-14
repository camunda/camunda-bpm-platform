<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/identity-links/get-identity-link-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricIdentityLinksCount"
      tag = "Historic Identity Link Log"
      summary = "Get Identity Link Log Count"
      desc = "Queries for the number of historic identity link logs that fulfill the given
              parameters. Takes the same parameters as the
              [Get Identity-Link-Logs](${docsUrl}/reference/rest/history/identity-links/get-identity-link-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/history-identity-link.ftl" >
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
                       "summary": "GET `/history/identity-link-log/count?taskId=aTaskId`",
                       "description": "GET `/history/identity-link-log/count?taskId=aTaskId`",
                       "value": {
                         "count": 2
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>