<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-set-job-priority/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setJobPriorityJobDefinition"
      tag = "Job Definition"
      summary = "Set Job Definition Priority by Id"
      desc = "Sets an overriding execution priority for jobs with the given definition id.
              Optionally, the priorities of all the definitions' existing jobs are
              updated accordingly. The priority can be reset by setting it to
              `null`, meaning that a new job's priority will not be determined based
              on its definition's priority any longer. See the
              [user guide on job prioritization](${docsUrl}/user-guide/process-engine/the-job-executor/#set-job-definition-priorities-via-managementservice-api)
              for details."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job definition to be updated."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobDefinitionPriorityDto"
      examples = ['"example-1": {
                     "summary": "PUT `/job-definition/aJobDefId/jobPriority`",
                     "value": {
                       "priority": 10,
                       "includeJobs": true
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Job definition with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The retries could not be set successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>