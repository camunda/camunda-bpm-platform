<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/get/index.html -->
{
  <@lib.endpointInfo
      id = "getJob"
      tag = "Job"
      summary = "Get Job"
      desc = "Retrieves a job by id, according to the `Job` interface in the engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job to be retrieved."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/job/aJobId`",
                       "value": {
                         "id": "aJobId",
                         "jobDefinitionId": "f9eec330-e3ff-11e8-8f7d-e4a7a094a9d6",
                         "dueDate": "2018-07-17T17:00:00+0200",
                         "processInstanceId": "aProcessInstanceId",
                         "processDefinitionId": "timer:1:f9ee9c1f-e3ff-11e8-8f7d-e4a7a094a9d6",
                         "processDefinitionKey": "timer",
                         "executionId": "anExecutionId",
                         "retries": 0,
                         "exceptionMessage": "An exception Message",
                         "failedActivityId": "anActivityId",
                         "suspended": false,
                         "priority": 10,
                         "tenantId": null,
                         "createTime": "2018-05-05T17:00:00+0200",
                         "batchId": "aBatchId"
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Job with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>