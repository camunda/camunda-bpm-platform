<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "queryExternalTasks"
      tag = "External Task"
      summary = "Get List (POST)"
      desc = "Queries for external tasks that fulfill given parameters in the form of a JSON object.

              This method is slightly more powerful than the
              [Get External Tasks](${docsUrl}/reference/rest/external-task/get-query/) method because it allows to
              specify a hierarchical result sorting." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExternalTaskQueryDto"
      examples = ['"example-1": {
                       "summary": "POST /external-task",
                       "value": {
                         "processDefinitionId": "aProcessDefinitionId",
                         "sorting": [
                           {
                             "sortBy": "processDefinitionKey",
                             "sortOrder": "asc"
                           },
                           {
                             "sortBy": "lockExpirationTime",
                             "sortOrder": "desc"
                           },
                           {
                             "sortBy": "createTime",
                             "sortOrder": "asc"
                           }
                         ]
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ExternalTaskDto"
        array = true
        desc = "Request successful. The Response is a JSON array of external task objects."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
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
                           "processDefinitionId": "aProcessDefinitionId",
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