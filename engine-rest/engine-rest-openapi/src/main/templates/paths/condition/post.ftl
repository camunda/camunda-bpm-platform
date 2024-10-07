<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "evaluateCondition"
      tag = "Condition"
      summary = "Evaluate"
      desc = "Triggers evaluation of conditions for conditional start event(s). 
              Internally this maps to the engines condition evaluation builder method ConditionEvaluationBuilder#evaluateStartConditions(). 
              For more information see the [Conditional Start Events](${docsUrl}/reference/bpmn20/events/conditional-events/#conditional-start-event)
              section of the [BPMN 2.0 Implementation Reference](${docsUrl}/reference/bpmn20/)." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "EvaluationConditionDto"
      examples = ['"example-1": {
                      "summary": "POST /condition",
                       "description": "The content of the Request Body",
                       "value": {
                         "variables" : {
                            "temperature" : {"value" : 24, "type": "Integer",
                                          "valueInfo" : { "transient" : true } },
                            "city" : {"value" : "Parma", "type": "String"}
                            },
                         "businessKey" : "aBusinessKey",
                         "tenantId" : "aTenantId"
                         }
                      }']/>

  "responses": {
    <@lib.response
        code = "200"
        dto = "ProcessInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                        "summary": "Status 200 Response",
                        "description": "The Response content of a status 200",
                        "value": [{
                          "links": [],
                          "id": "aProcInstId",
                          "definitionId": "aProcDefId",
                          "definitionKey": "aProcDefKey",
                          "businessKey": "aBusinessKey",
                          "caseInstanceId": null,
                          "ended": false,
                          "suspended": false,
                          "tenantId": "aTenantId"
                        },
                        {
                          "links": [],
                          "id": "anotherId",
                          "definitionId": "aProcDefId",
                          "definitionKey": "aProcDefKey",
                          "businessKey": "aBusinessKey",
                          "caseInstanceId": null,
                          "ended": false,
                          "suspended": false,
                          "tenantId": aTenantId
                        }]
                    }']/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "If both tenantId and withoutTenantId are supplied."/>

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "If the user is not allowed to start the process instance of the process definition, which start condition was evaluated to `true`."/>

  }
}

</#macro>
