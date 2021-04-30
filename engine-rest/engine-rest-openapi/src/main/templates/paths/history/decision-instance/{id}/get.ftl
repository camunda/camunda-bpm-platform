<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-instance/get-decision-instance/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricDecisionInstance"
      tag = "Historic Decision Instance"
      summary = "Get Historic Decision Instance"
      desc = "Retrieves a historic decision instance by id, according to the 
              `HistoricDecisionInstance` interface in the engine."
  />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the historic decision instance to be retrieved."
    />
      
    <#assign last = true >
    <#include "/lib/commons/historic-decision-instance-single-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricDecisionInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request including inputs and outputs",
                       "description": "GET `/history/decision-instance/aDecisionInstId?includeInput=true&includeOutputs=true`",
                       "value": {
                         "activityId": "assignApprover",
                         "activityInstanceId": "assignApprover:67e9de1e-579d-11e5-9848-f0def1e59da8",
                         "collectResultValue": null,
                         "decisionDefinitionId": "invoice-assign-approver:1:4c864d79-579d-11e5-9848-f0def1e59da8",
                         "decisionDefinitionKey": "invoice-assign-approver",
                         "decisionDefinitionName": "Assign Approver",
                         "evaluationTime": "2015-09-10T11:22:06.000+0200",
                         "removalTime": null,
                         "id": "67ea2c3f-579d-11e5-9848-f0def1e59da8",
                         "inputs": [
                           {
                             "clauseId": "clause1",
                             "clauseName": "Invoice Amount",
                             "decisionInstanceId": "67ea2c3f-579d-11e5-9848-f0def1e59da8",
                             "errorMessage": null,
                             "id": "67ea2c41-579d-11e5-9848-f0def1e59da8",
                             "type": "Double",
                             "createTime": "2015-09-10T11:22:06.000+0200",
                             "removalTime": null,
                             "rootProcessInstanceId": "aRootProcessInstanceId",
                             "value": 123.0,
                             "valueInfo": {}
                           },
                           {
                             "clauseId": "clause2",
                             "clauseName": "Invoice Category",
                             "decisionInstanceId": "67ea2c3f-579d-11e5-9848-f0def1e59da8",
                             "errorMessage": null,
                             "id": "67ea2c40-579d-11e5-9848-f0def1e59da8",
                             "type": "String",
                             "createTime": "2015-09-10T11:22:06.000+0200",
                             "removalTime": null,
                             "rootProcessInstanceId": "aRootProcessInstanceId",
                             "value": "Misc",
                             "valueInfo": {}
                           }
                         ],
                         "outputs": [
                           {
                             "clauseId": "clause3",
                             "clauseName": "Approver Group",
                             "decisionInstanceId": "67ea2c3f-579d-11e5-9848-f0def1e59da8",
                             "errorMessage": null,
                             "id": "67ea2c42-579d-11e5-9848-f0def1e59da8",
                             "ruleId": "DecisionRule_1of5a87",
                             "ruleOrder": 1,
                             "type": "String",
                             "createTime": "2015-09-10T11:22:06.000+0200",
                             "removalTime": null,
                             "rootProcessInstanceId": "aRootProcessInstanceId",
                             "value": "accounting",
                             "valueInfo": {},
                             "variableName": "result"
                           }
                         ],
                         "processDefinitionId": "invoice:1:4c6e3197-579d-11e5-9848-f0def1e59da8",
                         "processDefinitionKey": "invoice",
                         "processInstanceId": "67e98fec-579d-11e5-9848-f0def1e59da8",
                         "rootProcessInstanceId": "f8259e5d-ab9d-11e8-8449-e4a7a094a9d6",
                         "caseDefinitionId": null,
                         "caseDefinitionKey": null,
                         "caseInstanceId": null,
                         "tenantId": null,
                         "userId": null,
                         "rootDecisionInstanceId": null,
                         "decisionRequirementsDefinitionId": null,
                         "decisionRequirementsDefinitionKey": null
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Historic decision instance with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>