<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/put-activate-suspend-by-id/index.html -->
{
  <@lib.endpointInfo
      id = "updateJobSuspensionState"
      tag = "Job"
      summary = "Activate/Suspend Job By Id"
      desc = "Activates or suspends a given job by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job to activate or suspend."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SuspensionStateDto" <#-- Note: The java code uses JobSuspensionStateDto, but this endpoint only requires one attribute. As JobSuspensionStateDto is quite complex it could be confusing to the user to use it here.-->
      examples = ['"example-1": {
                     "summary": "PUT `/job/aJobId/suspended`",
                     "value": {
                       "suspended": true
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
        last = true
    />

  }

}
</#macro>