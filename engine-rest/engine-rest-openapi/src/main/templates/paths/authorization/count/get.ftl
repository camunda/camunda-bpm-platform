<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getAuthorizationCount"
      tag = "Authorization"
      summary = "Get Authorization Count"
      desc = "Queries for authorizations using a list of parameters and retrieves the count."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/authorization-query-params.ftl" >
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
                       "summary": "Status 200.",
                       "description": "GET `/authorization/count?userIdIn=jonny1,jonny2`",
                       "value": {
                         "count": 2
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy` is specified. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>