<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/put-set-job-retries/index.html -->
{
  <@lib.endpointInfo
      id = "setJobRetries"
      tag = "Job"
      summary = "Set Job Retries"
      desc = "Sets the retries of the job to the given number of retries by id."
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
      dto = "JobRetriesDto"
      examples = ['"example-1": {
                     "summary": "PUT `/job/aJobId/retries`",
                     "value": {
                       "retries": 3,
                       "dueDate": "2017-04-06T13:57:45.000+0200"
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
        desc = "The retries could not be set successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>