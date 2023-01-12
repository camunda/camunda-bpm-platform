<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/post-set-job-retries/index.html -->
{
  <@lib.endpointInfo
      id = "setJobRetriesAsyncOperation"
      tag = "Job"
      summary = "Set Job Retries Async (POST)"
      desc = "Create a batch to set retries of jobs asynchronously."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetJobRetriesDto"
      examples = ['"example-1": {
                     "summary": "POST `/job/retries`",
                     "value": {
                       "retries" : 5,
                       "dueDate": "2017-04-06T13:57:45.000+0200",
                       "jobIds": ["aJob","secondJob"],
                       "jobQuery": {
                         "dueDates":
                           [
                             {
                               "operator": "gt",
                               "value": "2012-07-17T17:00:00.000+0200"
                              },
                             {
                               "operator": "lt",
                               "value": "2012-07-17T18:00:00.000+0200"
                             }
                           ]
                       }
                      }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 OK",
                       "description": "POST `/job/retries`",
                       "value": {
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "totalJobs": 10,
                         "batchJobsPerSeed": 100,
                         "jobsCreated": 10,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "aSeedJobDefinitionId",
                         "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                         "batchJobDefinitionId": "aBatchJobDefinitionId",
                         "suspened": false,
                         "tenantId": "aTenantId",
                         "createUserId": "userId"
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if neither
                processInstanceIds nor processInstanceQuery is present. Or if the
                retry count is not specified. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}

</#macro>