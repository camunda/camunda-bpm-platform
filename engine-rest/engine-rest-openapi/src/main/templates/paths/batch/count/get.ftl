<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getBatchesCount"
      tag = "Batch"
      summary = "Get List Count"
      desc = "Requests the number of batches that fulfill the query criteria.
              Takes the same filtering parameters as the [Get Batches](${docsUrl}/reference/rest/batch/get-query/) method." />

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