<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/post-external-task-log-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricExternalTaskLogs"
      tag = "Historic External Task Log"
      summary = "Get External Task Logs (POST)"
      desc = "Queries for historic external task logs that fulfill the given parameters.
              This method is slightly more powerful than the
              [Get External Task Logs](${docsUrl}/reference/rest/history/external-task-log/get-external-task-log-query/)
              method because it allows filtering by historic external task logs
              values of the different types `String`, `Number` or `Boolean`."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricExternalTaskLogQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/external-task-log`",
                     "value": {
                       "externalTaskId": "anExternalTaskId"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricExternalTaskLogDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/external-task-log`",
                       "description": "POST `/history/external-task-log`",
                       "value": [
                         {
                           "id": "someId",
                           "timestamp": "2017-01-15T15:22:20.000+0200",
                           "externalTaskId": "anExternalTaskId",
                           "topicName": "aTopicName",
                           "workerId": "aWorkerId",
                           "retries": 3,
                           "priority": 5,
                           "errorMessage": "An error occured!",
                           "activityId": "externalServiceTask",
                           "activityInstanceId": "externalServiceTask:15",
                           "executionId": "anExecutionId",
                           "processInstanceId": "aProcessInstanceId",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "tenantId": null,
                           "creationLog": false,
                           "failureLog": true,
                           "successLog": false,
                           "deletionLog": false,
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>