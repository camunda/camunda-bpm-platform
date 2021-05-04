<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/history-cleanup/get-history-cleanup-job/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "findCleanupJob"
      tag = "History Cleanup"
      deprecated = true
      summary = "Find clean up history job (GET)"
      desc = "**Deprecated!** Use `GET /history/cleanup/jobs` instead.

              Finds history cleanup job (See
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup))."
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/cleanup/job`",
                       "description": "GET `/history/cleanup/job`",
                       "value": {
                         "id": "074bd92a-1a95-11e7-8ceb-34f39ab71d4e",
                         "jobDefinitionId": null,
                         "processInstanceId": null,
                         "processDefinitionId": null,
                         "processDefinitionKey": null,
                         "executionId": null,
                         "exceptionMessage": null,
                         "retries": 3,
                         "dueDate": "2017-04-06T13:57:45.000+0200",
                         "suspended": false,
                         "priority": 0,
                         "tenantId": null,
                         "createTime": "2017-05-05T17:00:00+0200"
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "History clean up job does not exist."
        last = true
    />

  }

}
</#macro>