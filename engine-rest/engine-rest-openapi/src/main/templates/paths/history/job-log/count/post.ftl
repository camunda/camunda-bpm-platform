<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/post-job-log-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricJobLogsCount"
      tag = "Historic Job Log"
      summary = "Get Job Log Count (POST)"
      desc = "Queries for the number of historic job logs that fulfill the given parameters.
              This method takes the same message body as the
              [Get Job Logs (POST)](${docsUrl}/reference/rest/history/job-log/post-job-log-query/)
              method and therefore it is slightly more powerful than the
              [Get Job Log Count](${docsUrl}/reference/rest/history/job-log/get-job-log-query-count/)
              method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricJobLogQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/job-log/count`",
                     "value": {
                       "jobId": "aJobId"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/job-log/count`",
                       "description": "POST `/history/job-log/count`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>