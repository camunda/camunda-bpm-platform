<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionDefinitions"
      tag = "Decision Definition"
      summary = "Get List"
      desc = "Queries for decision definitions that fulfill given parameters.
              Parameters may be the properties of decision definitions, such as the name, key or version.
              The size of the result set can be retrieved by using
              the [Get Decision Definition Count](${docsUrl}/reference/rest/decision-definition/get-query-count/) method." />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/decision-definition.ftl" >

    <#assign last = false >
    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >

    <@lib.parameters
        object = params
        last = true />

  ],
  "responses" : {
    <@lib.response
        code = "200"
        dto = "DecisionDefinitionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/decision-definition?key=dish-decision&sortBy=category&sortOrder=asc`",
                       "value": [
                         {
                            "id": "dish-decision:1:c633e8a8-41b7-11e6-b0ef-00aa004d0001",
                            "key": "dish-decision",
                            "category": "http://camunda.org/schema/1.0/dmn",
                            "name": "Dish Decision",
                            "version": 1,
                            "resource": "drd-dish-decision.dmn",
                            "deploymentId": "c627175e-41b7-11e6-b0ef-00aa004d0001",
                            "decisionRequirementsDefinitionId":"dish:1:c633c195-41b7-11e6-b0ef-00aa004d0001",
                            "decisionRequirementsDefinitionKey":"dish",
                            "tenantId": null,
                            "versionTag": null,
                            "historyTimeToLive": 5
                         }
                       ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid,
                for example if a `sortOrder` parameter is supplied, but no `sortBy`.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}

</#macro>