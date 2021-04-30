<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/post-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryJobDefinitions"
      tag = "Job Definition"
      summary = "Get Job Definitions (POST)"
      desc = "Queries for job definitions that fulfill given parameters. This method is slightly
              more powerful than the
              [Get Job Definitions](${docsUrl}/reference/rest/job-definition/get-query/)
              method because it allows filtering by multiple job definitions of
              types `String`, `Number` or `Boolean`."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobDefinitionQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/job-definition`",
                     "value": {
                       "activityIdIn": [
                         "ServiceTask1",
                         "ServiceTask2"
                       ],
                       "sorting": [
                         {
                           "sortBy": "activityId",
                           "sortOrder": "asc"
                         },
                         {
                           "sortBy": "jobType",
                           "sortOrder": "asc"
                         }
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDefinitionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/job-definition`",
                       "description": "POST `/job-definition`",
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