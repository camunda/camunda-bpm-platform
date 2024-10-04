<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deliverMessage"
      tag = "Message"
      summary = "Correlate"
      desc = "Correlates a message to the process engine to either trigger a message start event or an intermediate message 
              catching event. Internally this maps to the engine's message correlation builder methods
              `MessageCorrelationBuilder#correlateWithResult()` and `MessageCorrelationBuilder#correlateAllWithResult()`.
              For more information about the correlation behavior, see the [Message Events](${docsUrl}/bpmn20/events/message-events/)
              section of the [BPMN 2.0 Implementation Reference](${docsUrl}/reference/bpmn20/)." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CorrelationMessageDto"
      examples = ['"example-1": {
                      "summary": "POST /message",
                      "description": "Correlate without result",
                      "value": {
                        "messageName" : "aMessage",
                        "businessKey" : "aBusinessKey",
                        "correlationKeys" : {
                          "aVariable" : {"value" : "aValue", "type": "String"}
                        },
                        "processVariables" : {
                          "aVariable" : {"value" : "aNewValue", "type": "String", 
                                          "valueInfo" : { "transient" : true }
                                        },
                          "anotherVariable" : {"value" : true, "type": "Boolean"}
                        }
                      }
                     },
                   "example-2": {
                      "summary": "POST /message",
                      "description": "Correlate with result",
                      "value": {
                        "messageName" : "aMessage",
                        "businessKey" : "aBusinessKey",
                        "correlationKeys" : {
                          "aVariable" : {"value" : "aValue", "type": "String"}
                        },
                        "processVariables" : {
                          "aVariable" : {"value" : "aNewValue", "type": "String",
                                          "valueInfo" : { "transient" : true }
                                        },
                          "anotherVariable" : {"value" : true, "type": "Boolean"}
                        },
                        "resultEnabled" : true
                      }
                     },
                   "example-3": {
                      "summary": "POST /message",
                      "description": "Correlate with result and variables",
                      "value": {
                        "messageName" : "aMessage",
                        "businessKey" : "aBusinessKey",
                        "correlationKeys" : {
                          "aVariable" : {"value" : "aValue", "type": "String"}
                        },
                        "processVariables" : {
                          "aVariable" : {"value" : "aNewValue", "type": "String",
                                          "valueInfo" : { "transient" : true }
                                        },
                          "anotherVariable" : {"value" : true, "type": "Boolean"}
                        },
                        "resultEnabled" : true,
                        "variablesInResultEnabled" : true
                      }
                     }']
                     />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "MessageCorrelationResultWithVariableDto"
        array = true
        desc = "Request successful. The property `resultEnabled` in the request body was `true`.
                The `variables` property is only returned, if the property variablesInResultEnable`
                was set to `true` in the request."
      examples = ['"example-1": {
                       "summary": "Status 200 Response.",
                       "description": "The Response content of a status 200. The property `variablesInResultEnabled` in the request body was `false` (Default).",
                       "value": [{
                         "resultType": "ProcessDefinition",
                         "execution": null,
                         "processInstance": {
                           "links": [],
                             "id": "aProcInstId",
                             "definitionId": "aProcDefId",
                             "definitionKey": "aProcDefKey",
                             "businessKey": "aKey",
                             "caseInstanceId": "aCaseInstId",
                             "ended": false,
                             "suspended": false,
                             "tenantId": "aTenantId"
                         }
                    }]
                   },
                   "example-2": {
                     "summary": "Status 200 Response.",
                     "description": "The Response content of a status 200. The property `variablesInResultEnabled` in the request body was `true`.",
                     "value": [{
                       "resultType": "Execution",
                       "execution": {
                         "id": "anExecutionId",
                         "processInstanceId": "aProcInstId",
                         "ended": false,
                         "tenantId": "aTenantId"
                       },
                       "processInstance": null,
                       "variables" : {
                         "aVariable" : {"value" : "aNewValue", "type": "String",
                                       "valueInfo" : { "transient" : true }
                                     },
                         "anotherVariable" : {"value" : true, "type": "Boolean"}
                       }
                     }]
                   }']/>

    <@lib.response
        code = "204"
        desc = "Request successful. The property `resultEnabled` in the request body was `false` (Default)." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if:
                * no `messageName` was supplied
                * both `tenantId` and `withoutTenantId` are supplied
                * the message has not been correlated to exactly one entity (execution or process definition)
                * the variable value or type is invalid, for example if the value could not be parsed to an Integer value or the passed variable type is not supported.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}

</#macro>
