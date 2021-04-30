<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-activate-suspend-by-proc-def-id/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "updateSuspensionStateJobDefinitions"
      tag = "Job Definition"
      summary = "Activate/Suspend Job Definitions"
      desc = "Activates or suspends job definitions with the given process definition id or process definition key."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobDefinitionsSuspensionStateDto"
      examples = ['"example-1": {
                     "summary": "suspend by process definition id",
                     "description": "PUT `/job-definition/suspended`",
                     "value": {
                       "processDefinitionId": "aProcessDefinitionId",
                       "suspended": true,
                       "includeJobs": true,
                       "executionDate": "2013-11-21T10:49:45.000+0200"
                     }
                   }',
                   '"example-2": {
                     "summary": "suspend by process definition key",
                     "description": "PUT `/job-definition/suspended`",
                     "value": {
                       "processDefinitionKey": "aProcessDefinitionKey",
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