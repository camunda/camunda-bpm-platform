<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/history-cleanup/get-history-cleanup-jobs/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "findCleanupJobs"
      tag = "History Cleanup"
      summary = "Find clean up history jobs (GET)"
      desc = "Finds history cleanup jobs (See
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup))."
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/cleanup/jobs`",
                       "description": "GET `/history/cleanup/jobs`",
                       "value": [
                         {
                           "id": "aJobId",
                           "jobDefinitionId": null,
                           "processInstanceId": null,
                           "processDefinitionId": null,
                           "processDefinitionKey": null,
                           "executionId": null,
                           "exceptionMessage": null,
                           "retries": 3,
                           "dueDate": "aDueDate",
                           "suspended": false,
                           "priority": 0,
                           "tenantId": null,
                           "createTime": "2018-05-05T17:00:00+0200"
                         },
                         {
                           "id": "anotherJobId",
                           "jobDefinitionId": null,
                           "processInstanceId": null,
                           "processDefinitionId": null,
                           "processDefinitionKey": null,
                           "executionId": null,
                           "exceptionMessage": null,
                           "retries": 3,
                           "dueDate": "anotherDueDate",
                           "suspended": false,
                           "priority": 0,
                           "tenantId": null,
                           "createTime": "2018-05-05T17:00:00+0200"
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "History clean up jobs are empty."
        last = true
    />

  }

}
</#macro>