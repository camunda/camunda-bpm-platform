<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-activate-suspend-by-id/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "updateSuspensionStateJobDefinition"
      tag = "Job Definition"
      summary = "Activate/Suspend Job Definition By Id"
      desc = "Activates or suspends a given job definition by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job definition to activate or suspend."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobDefinitionSuspensionStateDto"
      examples = ['"example-1": {
                     "summary": "PUT `/job-definition/aJobDefinitionId/suspended`",
                     "value": {
                       "suspended": true,
                       "includeJobs": true,
                       "executionDate": "2013-11-21T10:49:45.000+0200"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the request parameters are invalid, for example if the provided
                `executionDate` parameter doesn't have the expected format. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>