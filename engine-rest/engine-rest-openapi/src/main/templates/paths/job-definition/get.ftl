<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/get-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getJobDefinitions"
      tag = "Job Definition"
      summary = "Get Job Definitions"
      desc = "Queries for job definitions that fulfill given parameters.
              The size of the result set can be retrieved by using the
              [Get Job Definition Count](${docsUrl}/reference/rest/job-definition/get-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/job-definition-query-params.ftl" >
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
        dto = "JobDefinitionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/job-definition?activityIdIn=ServiceTask1,ServiceTask2`",
                       "description": "GET `/job-definition?activityIdIn=ServiceTask1,ServiceTask2`",
                       "value": [
                         {
                           "id": "aJobDefId",
                           "processDefinitionId": "aProcDefId",
                           "processDefinitionKey": "aProcDefKey",
                           "activityId": "ServiceTask1",
                           "jobType": "asynchronous-continuation",
                           "jobConfiguration": "",
                           "suspended": false,
                           "overridingJobPriority": 15,
                           "tenantId": null,
                           "deploymentId": "aDeploymentId"
                         },
                         {
                           "id": "aJobDefId",
                           "processDefinitionId": "aProcDefId",
                           "processDefinitionKey": "aProcDefKey",
                           "activityId": "ServiceTask2",
                           "jobType": "asynchronous-continuation",
                           "jobConfiguration": "",
                           "suspended": true,
                           "overridingJobPriority": null,
                           "tenantId": null,
                           "deploymentId": "aDeploymentId"
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