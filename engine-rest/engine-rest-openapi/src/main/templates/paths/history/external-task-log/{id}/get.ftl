<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/get-external-task-log/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricExternalTaskLog"
      tag = "Historic External Task Log"
      summary = "Get External Task Log"
      desc = "Retrieves a historic external task log by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the log entry."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricExternalTaskLogDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/history/external-task-log/someId`",
                       "value": {
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
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Historic external task log with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>