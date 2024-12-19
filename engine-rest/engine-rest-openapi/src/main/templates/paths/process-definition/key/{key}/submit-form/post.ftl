<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "submitFormByKey"
      tag = "Process Definition"
      summary = "Submit Start Form"
      desc = "Starts the latest version of the process definition which belongs to no tenant
              using a set of process variables and the business key.
              If the start event has Form Field Metadata defined, the process engine will perform backend validation
              for any form fields which have validators defined.
              See [Documentation on Generated Task Forms](${docsUrl}/user-guide/task-forms/#generated-task-forms)." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The key of the process definition to submit the form for."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "StartProcessInstanceFormDto"
      examples = ['"example-1": {
                     "summary": "POST `/process-definition/key/aProcessDefinitionKey/submit-form`",
                     "value": {
                       "variables": {
                         "aVariable" : {
                             "value" : "aStringValue",
                             "type": "String"
                         },
                         "anotherVariable" : {
                           "value" : true,
                           "type": "Boolean"
                         }
                       },
                      "businessKey" : "myBusinessKey"
                     }
                   }'
                 ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
                       "description": "POST `/process-definition/key/aProcessDefinitionKey/submit-form`",
                       "value": {
                         "links": [
                           {
                             "method": "GET",
                             "href":"http://localhost:8080/rest-test/process-instance/anId",
                             "rel":"self"
                           }
                         ],
                         "id":"anId",
                         "definitionId":"aProcessDefinitionId",
                         "definitionKey":"aProcessDefinitionKey",
                         "businessKey":"myBusinessKey",
                         "caseInstanceId": null,
                         "tenantId":null,
                         "ended":false,
                         "suspended":false
                       }
                     }'
                   ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The instance could not be created due to an invalid variable value,
                for example if the value could not be parsed to an `Integer` value or
                the passed variable type is not supported.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Process definition with given key does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The instance could not be created successfully.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />


  }
}

</#macro>
