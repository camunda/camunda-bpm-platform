<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricDecisionInstances"
      tag = "Historic Decision Instance"
      summary = "Get Historic Decision Instances"
      desc = "Queries for historic decision instances that fulfill the given parameters. 
              The size of the result set can be retrieved by using the 
              [Get Historic Decision Instance Count](${docsUrl}/reference/rest/history/decision-instance/get-decision-instance-query-count/) 
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/historic-decision-instance-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/historic-decision-instance-single-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricDecisionInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request including inputs and outputs",
                       "description": "GET `/history/decision-instance?includeInputs=true&includeOutputs=true`",
                       "value": [
                         {
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
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>