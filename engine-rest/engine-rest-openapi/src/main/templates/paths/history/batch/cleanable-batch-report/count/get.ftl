<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get-cleanable-batch-report-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getCleanableHistoricBatchesReportCount"
      tag = "Historic Batch"
      summary = "Get Cleanable Batch Report Count"
      desc = "Queries for the number of report results about a historic batch operations relevant
              to history cleanup (see
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)
              ).
              Takes the same parameters as the
              [Get Cleanable Batch Report](${docsUrl}/reference/rest/history/batch/get-cleanable-batch-report/)
              method."
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/batch/cleanable-batch-report/count`",
                       "description": "GET `/history/batch/cleanable-batch-report/count`",
                       "value": {
                         "count": 10
                       }
                     }']
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>