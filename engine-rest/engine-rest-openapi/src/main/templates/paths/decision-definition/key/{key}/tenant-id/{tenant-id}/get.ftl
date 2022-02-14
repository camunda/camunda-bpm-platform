<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionDefinitionByKeyAndTenantId"
      tag = "Decision Definition"
      summary = "Get Decision Definition By Key And Tenant Id"
      desc = "Retrieves the latest version of the decision definition for tenant" />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the decision definition (the latest version thereof) to be retrieved." />

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the tenant the decision definition belongs to." />


  ],
  "responses" : {
    <@lib.response
        code = "200"
        dto = "DecisionDefinitionDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/decision-definition/key/dish-decision/tenant-id/aTenantId`",
                       "value": {
                            "id": "aDecisionDefinitionId",
                            "key": "dish-decision",
                            "category": "http://camunda.org/schema/1.0/dmn",
                            "name": "Dish Decision",
                            "version": 1,
                            "resource": "drd-dish-decision.dmn",
                            "deploymentId": "c627175e-41b7-11e6-b0ef-00aa004d0001",
                            "decisionRequirementsDefinitionId":"dish:1:c633c195-41b7-11e6-b0ef-00aa004d0001",
                            "decisionRequirementsDefinitionKey":"dish",
                            "tenantId": aTenantId,
                            "versionTag": null,
                            "historyTimeToLive": 5
                         }
                       }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Decision definition with given key does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}
</#macro>