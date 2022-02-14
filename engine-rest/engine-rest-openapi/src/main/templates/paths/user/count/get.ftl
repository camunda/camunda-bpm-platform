<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getUserCount"
      tag = "User"
      summary = "Get List Count"
      desc = "Queries for the number of deployments that fulfill given parameters. Takes the same parameters as the
              [Get Users](${docsUrl}/reference/rest/user/get-query/) method." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/user-query-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/user/count?firstName=John`",
                       "value": {
                         "count": 2
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example, if an invalid operator for variable
                comparison is used. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
    }
}
</#macro>