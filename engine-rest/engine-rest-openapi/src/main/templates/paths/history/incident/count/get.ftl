<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/incident/get-incident-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricIncidentsCount"
      tag = "Historic Incident"
      summary = "Get Incident Count"
      desc = "Queries for the number of historic incidents that fulfill the given parameters.
              Takes the same parameters as the
              [Get Incidents](${docsUrl}/reference/rest/history/incident/get-incident-query/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/historic-incident-query-params.ftl" >
    <#assign last = true >
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
                       "summary": "GET `/history/incident/count?processInstanceId=aProcInstId`",
                       "description": "GET `/history/incident/count?processInstanceId=aProcInstId`",
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