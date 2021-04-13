<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "evaluateDecisionByKeyAndTenant"
      tag = "Decision Definition"
      summary = "Evaluate By Key And Tenant"
      desc = "Evaluates the latest version of the decision definition for tenant.
              The input values of the decision have to be supplied in the request body." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the decision definition (the latest version thereof) to be evaluated." />

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the tenant the decision definition belongs to." />

  ],

    <@lib.requestBody
        mediaType = "application/json"
        dto = "EvaluateDecisionDto"
        examples = [ '"example-1": {
                       "summary": "POST /decision-definition/key/aKey/tenand-id/aTenantId/evaluate",
                       "value": {
                         "variables" : {
                           "amount" : { "value" : 600, "type" : "Double" },
                           "invoiceCategory" : { "value" : "Misc", "type" : "String" }
                         }
                       }
                     }'
      ] />

  "responses" : {
    <@lib.response
        code = "200"
        dto = "VariableValueDto"
        array = true
        additionalProperties = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for POST `/decision-definition/akey/aKey/tenand-id/aTenantId/evaluate`",
                       "value": [
                         {
                           "result": { "value" : "management", "type" : "String", "valueInfo" : null }
                         }
                       ]
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