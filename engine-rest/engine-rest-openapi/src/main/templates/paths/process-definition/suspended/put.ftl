<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateProcessDefinitionSuspensionState"
      tag = "Process Definition"
      summary = "Activate/Suspend By Key"
      desc = "Activates or suspends process definitions with the given process definition key." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessDefinitionSuspensionStateDto"
      requestDesc = "**Note**: Unallowed property is `processDefinitionId`."
      examples = ['"example-1": {
                     "summary": "PUT `/process-definition/suspended`",
                     "value": {
                       "processDefinitionKey" : "aProcessDefinitionKey",
                       "suspended" : true,
                       "includeProcessInstances" : true,
                       "executionDate" : "2013-11-21T10:49:45T14:42:45"
                     }
                   }'
                 ] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid,
                for example if the provided `executionDate` parameter doesn't have the expected format or
                if the `processDefinitionKey` parameter is `null`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given key does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>