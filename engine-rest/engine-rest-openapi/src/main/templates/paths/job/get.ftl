<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/get-query/index.html -->
{
  <@lib.endpointInfo
      id = "getJobs"
      tag = "Job"
      summary = "Get Jobs"
      desc = "Queries for jobs that fulfill given parameters.
              The size of the result set can be retrieved by using the [Get Job
              Count](${docsUrl}/reference/rest/job/get-query-count/) method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/job-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/job/count?dueDates=gt_2012-07-17T17:00:00.000+0200,lt_2012-07-17T18:00:00.000+0200&createTimes=gt_2012-05-05T10:00:00.000+0200,lt_2012-07-16T15:00:00.000+0200`",
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