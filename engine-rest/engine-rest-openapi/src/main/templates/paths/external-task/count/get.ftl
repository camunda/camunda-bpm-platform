<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getExternalTasksCount"
      tag = "External Task"
      summary = "Get List Count"
      desc = "Queries for the number of external tasks that fulfill given parameters. Takes the same parameters as the
              [Get External Tasks](${docsUrl}/reference/rest/external-task/get-query/) method." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/external-task-query-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /external-task/count?topicName=aTopic",
                       "value": {
                         "count": 42
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>