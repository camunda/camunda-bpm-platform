<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/get/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getJobDefinition"
      tag = "Job Definition"
      summary = "Get Job Definition"
      desc = "Retrieves a job definition by id, according to the `JobDefinition` interface in the engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job definition to be retrieved."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "JobDefinitionDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/job-definition/aJobDefinitionId`",
                       "description": "GET `/job-definition/aJobDefinitionId`",
                       "value": {
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
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Job definition with given id does not exist.  See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>