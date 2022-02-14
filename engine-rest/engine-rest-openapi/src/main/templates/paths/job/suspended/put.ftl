<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/put-activate-suspend-by-proc-def-key/index.html -->
<#-- This file is actually based on four doc files found under put-activate-suspend-by-* as they all describe the same endpoint -->
{
  <@lib.endpointInfo
      id = "updateSuspensionStateBy"
      tag = "Job"
      summary = "Activate/Suspend Jobs"
      desc = "Activates or suspends jobs matching the given criterion.
              This can only be on of:
              * `jobDefinitionId`
              * `processDefinitionId`
              * `processInstanceId`
              * `processDefinitionKey`"
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobSuspensionStateDto"
      examples = [
      '"example-1": {
                     "summary": "Activates or suspends jobs with the given job definition id. PUT `/job/suspended`",
                     "value": {
                       "jobDefinitionId" : "aJobDefinitionId",
                       "suspended": true
                     }
                   }',
      '"example-2": {
                     "summary": "Activates or suspends jobs with the given process definition id. PUT `/job/suspended`",
                     "value": {
                       "processDefinitionId" : "aProcessDefinitionId",
                       "suspended": true
                     }
                   }',
      '"example-3": {
                     "summary": "Activates or suspends jobs with the given process instance id. PUT `/job/suspended`",
                     "value": {
                       "processInstanceId" : "aProcessInstanceId",
                       "suspended": true
                     }
                   }',
      '"example-4": {
                     "summary": "Activates or suspends jobs with the given process definition key. PUT `/job/suspended`",
                     "value": {
                       "processDefinitionKey": "aProcessDefinitionKey",
                       "suspended": true
                     }
                   }'


      ]
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if the request parameters are invalid, for example, if `jobDefinitionId` and
                `processDefinitionId` are both specified.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>