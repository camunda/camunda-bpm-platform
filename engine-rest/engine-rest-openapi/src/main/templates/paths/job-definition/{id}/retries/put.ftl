<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-set-job-retries/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setJobRetriesJobDefinition"
      tag = "Job Definition"
      summary = "Set Job Retries By Job Definition Id"
      desc = "Sets the number of retries of all **failed** jobs associated with the given job
              definition id."
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
      dto = "RetriesDto"
      examples = ['"example-1": {
                     "summary": "PUT `/job-definition/aJobDefId/retries`",
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
