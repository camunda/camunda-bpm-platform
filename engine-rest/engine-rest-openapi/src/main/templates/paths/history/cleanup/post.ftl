<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/history-cleanup/post-history-cleanup/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "cleanupAsync"
      tag = "History Cleanup"
      summary = "Clean up history (POST)"
      desc = "Schedules asynchronous history cleanup (See
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)).

              **Note:** This endpoint will return at most a single history cleanup job.
              Since version `7.9.0` it is possible to configure multiple
              [parallel history cleanup jobs](${docsUrl}/user-guide/process-engine/history/#parallel-execution). Use
              [`GET /history/cleanup/jobs`](${docsUrl}/reference/rest/history/history-cleanup/get-history-cleanup-jobs)
              to find all the available history cleanup jobs."
  />

  "parameters" : [

      <@lib.parameter
          name = "immediatelyDue"
          location = "query"
          type = "boolean"
          desc = "When true the job will be scheduled for nearest future. When `false`, the job will be
                  scheduled for next batch window start time. Default is `true`."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/cleanup?immediatelyDue=false`",
                       "description": "POST `/history/cleanup?immediatelyDue=false`",
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
                         "createTime": "2017-04-01T09:45:15.039+0100"
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid or the engine
                does not participate in history cleanup. See
                [Cleanup Execution Participation per Node](${docsUrl}/user-guide/process-engine/history/#cleanup-execution-participation-per-node)."
        last = true
    />

  }

}
</#macro>