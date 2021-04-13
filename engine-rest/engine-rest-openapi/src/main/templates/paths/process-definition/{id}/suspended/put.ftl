<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateProcessDefinitionSuspensionStateById"
      tag = "Process Definition"
      summary = "Activate/Suspend By Id"
      desc = "Activates or suspends a given process definition by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process definition to activate or suspend."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessDefinitionSuspensionStateDto"
      requestDesc = "**Note**: Unallowed properties are `processDefinitionId` and `processDefinitionKey`."
      examples = ['"example-1": {
                     "summary": "PUT `/process-definition/aProcessDefinitionId/suspended`",
                     "value": {
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