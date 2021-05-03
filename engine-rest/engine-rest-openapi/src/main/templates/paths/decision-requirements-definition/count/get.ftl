<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionRequirementsDefinitionsCount"
      tag = "Decision Requirements Definition"
      summary = "Get Decision Requirements Definition Count"
      desc = "Requests the number of decision requirements definitions that fulfill the query
              criteria.
              Takes the same filtering parameters as the
              [Get Decision Requirements Definitions](${docsUrl}/reference/rest/decision-requirements-definition/get-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/decision-requirements-definition.ftl" >
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
                       "summary": "GET `/decision-requirements-definition/count?key=dish`",
                       "description": "GET `/decision-requirements-definition/count?key=dish`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if
                a `sortOrder` parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>