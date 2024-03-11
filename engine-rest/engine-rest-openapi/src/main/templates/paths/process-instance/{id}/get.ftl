<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getProcessInstance"
      tag = "Process Instance"
      summary = "Get Process Instance"
      desc = "Retrieves a process instance by id, according to the `ProcessInstance` interface in the engine." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process instance to be retrieved." />

  ],
  "responses": {

    <@lib.response
        code = "200"
        desc = "Request successful." 
        dto = "ProcessInstanceDto"
        examples = ['"example-1": {
                       "summary": "GET `/process-instance/aProcessInstanceId`",
                       "value": {
                         "id":"aProcessInstanceId",
                         "definitionId":"aProcDefId",
                         "definitionKey":"aProcDefKey",
                         "businessKey":"aKey",
                         "caseInstanceId":"aCaseInstanceId",
                         "ended":false,
                         "suspended":false,
                         "tenantId":null
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process instance with given id does not exist. See the 
               [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format. " />

  }
}
</#macro>
