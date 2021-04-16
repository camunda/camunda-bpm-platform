<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getHistoricActivityInstance"
      tag = "Historic Activity Instance"
      summary = "Get"
      desc = "Retrieves a historic activity instance by id, according to the `HistoricActivityInstance` interface in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the historic activity instance to be retrieved." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "HistoricActivityInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/activity-instance/aActivityInstId`",
                       "value": {
                         "id": "aActivityInstId",
                         "activityId": "anActivity",
                         "activityName": "anActivityName",
                         "activityType": "userTask",
                         "assignee": "peter",
                         "calledProcessInstanceId": "aHistoricCalledProcessInstanceId",
                         "calledCaseInstanceId": null,
                         "canceled": true,
                         "completeScope": false,
                         "durationInMillis": 2000,
                         "endTime": "2013-04-23T18:42:43.000+0200",
                         "executionId": "anExecutionId",
                         "parentActivityInstanceId": "aHistoricParentActivityInstanceId",
                         "processDefinitionId": "aProcDefId",
                         "processInstanceId": "aProcInstId",
                         "startTime": "2013-04-23T11:20:43.000+0200",
                         "taskId": "aTaskId",
                         "tenantId":null,
                         "removalTime":"2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "aRootProcessInstanceId"
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Not Found
                Historic activity instance with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}

</#macro>