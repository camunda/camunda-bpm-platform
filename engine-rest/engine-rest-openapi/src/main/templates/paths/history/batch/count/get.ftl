<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricBatchesCount"
      tag = "Historic Batch"
      summary = "Get Historic Batch Count"
      desc = "Requests the number of historic batches that fulfill the query criteria.
              Takes the same filtering parameters as the
              [Get Historic Batches](${docsUrl}/reference/rest/history/batch/get-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/historic-batch-params.ftl" >
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
                       "description": "GET `/history/batch/count?type=aBatchType`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                Returned if some of the query parameters are invalid, for example if
                a `sortOrder` parameter is supplied, but no `sortBy`.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>