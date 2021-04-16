<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "evaluateDecisionById"
      tag = "Decision Definition"
      summary = "Evaluate By Id"
      desc = "Evaluates a given decision and returns the result.
              The input values of the decision have to be supplied in the request body." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the decision definition to be evaluated." />
  ],

    <@lib.requestBody
        mediaType = "application/json"
        dto = "EvaluateDecisionDto"
        examples = [ '"example-1": {
                       "summary": "POST /decision-definition/aDecisionDefinitionId/evaluate",
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
                       "description": "Response for POST `/decision-definition/aDecisionDefinitionId/evaluate`",
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
        desc = "Decision definition with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}
</#macro>