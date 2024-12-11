<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/post-query/index.html -->
{
  <@lib.endpointInfo
      id = "queryJobs"
      tag = "Job"
      summary = "Get Jobs (POST)"
      desc = "Queries for jobs that fulfill given parameters. This method is slightly more
              powerful than the [Get Jobs](${docsUrl}/reference/rest/job/get-query/)
              method because it allows filtering by multiple jobs of types `String`,
              `Number` or `Boolean`."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/job`",
                     "value": {
                       "dueDates": [
                         {
                           "operator": "gt",
                           "value": "2018-07-17T17:00:00.000+0200"
                         },
                         {
                           "operator": "lt",
                           "value": "2018-07-17T18:00:00.000+0200"
                         }
                       ],
                       "createTimes": [
                         {
                           "operator": "gt",
                           "value": "2012-05-05T10:00:00.000+0200"
                         },
                         {
                           "operator": "lt",
                           "value": "2012-07-16T15:00:00.000+0200"
                         }
                       ],
                       "sorting": [
                         {
                           "sortBy": "jobDueDate",
                           "sortOrder": "asc"
                         },
                         {
                           "sortBy": "jobRetries",
                           "sortOrder": "asc"
                         }
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "POST `/job`",
                       "value": [
                         {
                           "id": "aJobId",
                           "jobDefinitionId": "aJobDefinitionId",
                           "dueDate": "2018-07-17T17:05:00.000+0200",
                           "processInstanceId": "aProcessInstanceId",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aPDKey",
                           "executionId": "anExecutionId",
                           "retries": 0,
                           "exceptionMessage": "An exception Message",
                           "failedActivityId": "anActivityId",
                           "suspended": false,
                           "priority": 10,
                           "tenantId": null,
                           "createTime": "2018-05-05T17:00:00+0200",
                           "batchId": "aBatchId"
                         },
                         {
                           "id": "anotherJobId",
                           "jobDefinitionId": "anotherJobDefinitionId",
                           "dueDate": "2018-07-17T17:55:00.000+0200",
                           "processInstanceId": "aProcessInstanceId",
                           "processDefinitionId": "anotherPDId",
                           "processDefinitionKey": "anotherPDKey",
                           "executionId": "anotherExecutionId",
                           "retries": 0,
                           "exceptionMessage": "Another exception Message",
                           "failedActivityId": "anotherActivityId",
                           "suspended": true,
                           "priority": 8,
                           "tenantId": null,
                           "createTime": "2018-05-05T17:00:00+0200",
                           "batchId": null
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`, or if an invalid operator
                for due date comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>