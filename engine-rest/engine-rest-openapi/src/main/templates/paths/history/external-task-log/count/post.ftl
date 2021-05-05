<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/post-external-task-log-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricExternalTaskLogsCount"
      tag = "Historic External Task Log"
      summary = "Get External Task Log Count (POST)"
      desc = "Queries for the number of historic external task logs that fulfill the given
              parameters.
              This method takes the same message body as the
              [Get External Task Logs (POST)](${docsUrl}/reference/rest/history/external-task-log/post-external-task-log-query/)
              method and therefore it is slightly more powerful than the
              [Get External Task Log Count](${docsUrl}/reference/rest/history/external-task-log/get-external-task-log-query-count/)
              method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricExternalTaskLogQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/external-task-log/count`",
                     "value": {
                       "externalTaskId": "anExternalTaskId"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/external-task-log/count`",
                       "description": "POST `/history/external-task-log/count`",
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