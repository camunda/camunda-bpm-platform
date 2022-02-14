<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "queryHistoricActivityInstances"
      tag = "Historic Activity Instance"
      summary = "Get List (POST)"
      desc = "Queries for historic activity instances that fulfill the given parameters.
              The size of the result set can be retrieved by using the
              [Get Historic Activity Instance Count](${docsUrl}/reference/rest/history/activity-instance/get-activity-instance-query-count/) method." />

  "parameters" : [
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >
   ],

  <#assign requestMethod="POST"/>
  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricActivityInstanceQueryDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/history/activity-instance`",
                     "value": {
                                "activityType": "userTask",
                                "taskAssignee": "peter",
                                "sorting":
                                  [{"sortBy": "activityId",
                                  "sortOrder": "asc"
                                  },
                                  {"sortBy": "executionId",
                                  "sortOrder": "desc"
                                  }]
                              }
                   }'
                ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "HistoricActivityInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
                       "value": [
                                  {
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
                                    "id": "aHistoricActivityInstanceId",
                                    "parentActivityInstanceId": "aHistoricParentActivityInstanceId",
                                    "processDefinitionId": "aProcDefId",
                                    "processInstanceId": "aProcInstId",
                                    "startTime": "2013-04-23T11:20:43.000+0200",
                                    "taskId": "aTaskId",
                                    "tenantId":null,
                                    "removalTime":"2018-02-10T14:33:19.000+0200",
                                    "rootProcessInstanceId": "aRootProcessInstanceId"
                                  }
                                ]
                   }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if a sortOrder parameter is supplied, but no sortBy.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}
</#macro>