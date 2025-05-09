<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "startProcessInstanceByKeyAndTenantId"
      tag = "Process Definition"
      summary = "Start Instance"
      desc = "Instantiates a given process definition, starts the latest version of the process definition for tenant.
              Process variables and business key may be supplied in the request body." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the process definition (the latest version thereof) to be retrieved."/>

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the tenant the process definition belongs to."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "StartProcessInstanceDto"
      examples = ['"example-1": {
                     "summary": "Starting a process instance at its default initial activity",
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
                   }',
                   '"example-2": {
                     "summary": "Starting a process instance with variables in return",
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
                      "businessKey" : "myBusinessKey",
                      "withVariablesInReturn": true
                     }
                   }',
                   '"example-3": {
                     "summary": "Starting a process instance at two specific activities",
                     "value": {
                       "variables": {
                         "aProcessVariable" : {
                             "value" : "aStringValue",
                             "type": "String"
                         }
                       },
                      "businessKey" : "myBusinessKey",
                      "skipCustomListeners" : true,
                      "startInstructions" :
                        [
                          {
                            "type": "startBeforeActivity",
                            "activityId": "activityId",
                            "variables": {
                              "var": {
                                "value": "aVariableValue",
                                "local": false,
                                "type": "String"}
                            }
                          },
                          {
                            "type": "startAfterActivity",
                            "activityId": "anotherActivityId",
                            "variables": {
                              "varLocal": {
                                "value": "anotherVariableValue",
                                "local": true,
                                "type": "String"
                              }
                            }
                          }
                        ]
                      }
                   }'
                 ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessInstanceWithVariablesDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
                       "description": "Response for starting a process instance at its default initial activity",
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
                     }',
                     '"example-2": {
                       "summary": "Status 200 Response 2",
                       "description": "Response for starting a process instance with variables in return",
                       "value": {
                         "links": [
                           {
                             "method": "GET",
                             "href": "http://localhost:8080/rest-test/process-instance/aProcInstId",
                             "rel": "self"
                           }
                         ],
                         "id": "aProcInstId",
                         "definitionId": "aProcessDefinitionId",
                         "definitionKey":"aProcessDefinitionKey",
                         "businessKey": "myBusinessKey",
                         "caseInstanceId": null,
                         "ended": false,
                         "suspended": false,
                         "tenantId": null,
                         "variables": {
                           "anotherVariable": {
                               "type": "Boolean",
                               "value": true,
                               "valueInfo": {
                                 "transient" : true
                               }
                           },
                           "aVariable": {
                               "type": "String",
                               "value": "aStringValue",
                               "valueInfo": { }
                           }
                         }
                       }
                     }',
                     '"example-3": {
                       "summary": "Status 200 Response 3",
                       "description": "Response for starting a process instance at two specific activities",
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
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The instance could not be created due to an invalid variable value,
                for example if the value could not be parsed to an `Integer` value or
                the passed variable type is not supported.
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
