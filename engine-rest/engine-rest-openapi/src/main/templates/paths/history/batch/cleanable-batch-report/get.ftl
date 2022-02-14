<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get-cleanable-batch-report/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getCleanableHistoricBatchesReport"
      tag = "Historic Batch"
      summary = "Get Cleanable Batch Report"
      desc = "Retrieves a report about a historic batch operations relevant to history cleanup
              (see
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)
              ) so that you can tune the history time to live.
              These reports include the count of the finished batches, cleanable
              batches and type of the batch.
              The size of the result set can be retrieved by using the
              [Get Cleanable Batch Report Count](${docsUrl}/reference/rest/history/batch/get-cleanable-batch-report-count/)
              method.

              **Please note:**
              The history time to live for batch operations does not support [Multi-Tenancy](${docsUrl}/user-guide/process-engine/multi-tenancy.md).
              The report will return an information for all batch operations (for all tenants) if you have permissions
              to see the history.
              "
  />

  "parameters" : [
    <#assign sortByValues = [
        '"finished"'
    ]>
    <#assign last = false >
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CleanableHistoricBatchReportResultDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/batch/cleanable-batch-report`",
                       "description": "GET `/history/batch/cleanable-batch-report`

                                      An array containing finished batches information relevant to history cleanup.",
                       "value": [
                         {
                           "batchType": "instance-modification",
                           "historyTimeToLive": 5,
                           "finishedBatchCount": 100,
                           "cleanableBatchCount": 53
                         },
                         {
                           "batchType": "instance-deletion",
                           "historyTimeToLive": 5,
                           "finishedBatchCount": 1000,
                           "cleanableBatchCount": 13
                         }
                       ]
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