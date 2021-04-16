<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/put-set-job-priority/index.html -->
{
  <@lib.endpointInfo
      id = "setJobPriority"
      tag = "Job"
      summary = "Set Job Priority"
      desc = "Sets the execution priority of a job by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job to be updated."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "PriorityDto"
      examples = ['"example-1": {
                     "summary": "PUT `/job/aJobId/priority`",
                     "value": {
                       "priority": 10
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
        desc = "Job with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The priority could not be set successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>