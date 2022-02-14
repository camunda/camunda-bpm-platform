<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getExternalTasks"
      tag = "External Task"
      summary = "Get List"
      desc = "Queries for the external tasks that fulfill given parameters. Parameters may be static as well as dynamic
              runtime properties of executions. The size of the result set can be retrieved by using the
              [Get External Task Count](${docsUrl}/reference/rest/external-task/get-query-count/) method." />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/external-task-query-params.ftl" >

    <#assign sortByValues = [ '"id"', '"lockExpirationTime"', '"processInstanceId"', '"processDefinitionId"',
                              '"processDefinitionKey"', '"taskPriority"', '"tenantId"' ] >
    <#include "/lib/commons/sort-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ExternalTaskDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /external-task?topicName=aTopic",
                       "value": [
                         {
                           "activityId": "anActivityId",
                           "activityInstanceId": "anActivityInstanceId",
                           "errorMessage": "anErrorMessage",
                           "executionId": "anExecutionId",
                           "id": "anExternalTaskId",
                           "lockExpirationTime": "2015-10-06T16:34:42.000+0200",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "processInstanceId": "aProcessInstanceId",
                           "tenantId": null,
                           "retries": 3,
                           "suspended": false,
                           "workerId": "aWorkerId",
                           "topicName": "aTopic",
                           "priority": 9,
                           "businessKey": "aBusinessKey"
                         },
                         {
                           "activityId": "anotherActivityId",
                           "activityInstanceId": "anotherActivityInstanceId",
                           "errorMessage": "anotherErrorMessage",
                           "executionId": "anotherExecutionId",
                           "id": "anotherExternalTaskId",
                           "lockExpirationTime": "2015-10-06T16:34:42.000+0200",
                           "processDefinitionId": "anotherProcessDefinitionId",
                           "processDefinitionKey": "anotherProcessDefinitionKey",
                           "processInstanceId": "anotherProcessInstanceId",
                           "tenantId": null,
                           "retries": 1,
                           "suspended": false,
                           "workerId": "aWorkerId",
                           "topicName": "aTopic",
                           "priority": 3,
                           "businessKey": "aBusinessKey"
                         }
                       ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy`. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>