<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getProcessDefinitionsCount"
      tag = "Process Definition"
      summary = "Get List Count"
      desc = "Requests the number of process definitions that fulfill the query criteria.
              Takes the same filtering parameters as the [Get Definitions](${docsUrl}/reference/rest/process-definition/get-query/) method." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/process-definition-query-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response of GET `/process-definition/count?keyLike=Key&version=47`",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>