<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getBatchStatisticsCount"
      tag = "Batch"
      summary = "Get Statistics Count"
      desc = "Requests the number of batch statistics that fulfill the query criteria.
              Takes the same filtering parameters as the
              [Get Batch Statistics](${docsUrl}/reference/rest/batch/get-statistics-query/) method." />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/batch.ftl" >

    <@lib.parameters
        object = params
        last = true />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/batch/count?type=aBatchType`",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}
</#macro>