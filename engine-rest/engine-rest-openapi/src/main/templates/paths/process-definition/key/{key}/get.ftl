<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getProcessDefinitionByKey"
      tag = "Process Definition"
      summary = "Get"
      desc = "Retrieves the latest version of the process definition which belongs to no tenant according to the `ProcessDefinition` interface in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The key of the process definition (the latest version thereof) to be retrieved."/>
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessDefinitionDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response of GET `/process-definition/key/invoice`",
                       "value": {
                         "id": "invoice:1:c3a63aaa-2046-11e7-8f94-34f39ab71d4e",
                         "key": "invoice",
                         "category": "http://www.omg.org/spec/BPMN/20100524/MODEL",
                         "description": null,
                         "name": "Invoice Receipt",
                         "version": 1,
                         "resource": "invoice.v1.bpmn",
                         "deploymentId": "c398cd26-2046-11e7-8f94-34f39ab71d4e",
                         "diagram": null,
                         "suspended": false,
                         "tenantId": null,
                         "versionTag": null,
                         "historyTimeToLive": 5,
                         "startableInTasklist": true
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given `key` does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>