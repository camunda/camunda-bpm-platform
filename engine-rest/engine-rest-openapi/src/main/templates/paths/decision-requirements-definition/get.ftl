<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionRequirementsDefinitions"
      tag = "Decision Requirements Definition"
      summary = "Get Decision Requirements Definitions"
      desc = "Queries for decision requirements definitions that fulfill given parameters.
              Parameters may be the properties of decision requirements definitions, such as the name,
              key or version.  The size of the result set can be retrieved by using the
              [Get Decision Requirements Definition Count](${docsUrl}/reference/rest/decision-requirements-definition/get-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/decision-requirements-definition.ftl" >
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
        dto = "DecisionRequirementsDefinitionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/decision-requirements-definition?key=dish&sortBy=version&sortOrder=asc`",
                       "description": "GET `/decision-requirements-definition?key=dish&sortBy=version&sortOrder=asc`",
                       "value": [
                         {
                           "id": "dish:1:c633c195-41b7-11e6-b0ef-00aa004d0001",
                           "key": "dish",
                           "category": "drd-test",
                           "name": "Dish",
                           "version": 1,
                           "resource": "dish.dmn",
                           "deploymentId": "c627175e-41b7-11e6-b0ef-00aa004d0001",
                           "tenantId": null
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if
                a `sortOrder` parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>