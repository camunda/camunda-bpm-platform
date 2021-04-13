<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "evaluateDecisionByKey"
      tag = "Decision Definition"
      summary = "Evaluate By Key"
      desc = "Evaluates the latest version of the decision definition which belongs to no tenant.
              The input values of the decision have to be supplied in the request body." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The key of the decision definition (the latest version thereof) to be evaluated." />
  ],

    <@lib.requestBody
        mediaType = "application/json"
        dto = "EvaluateDecisionDto"
        examples = [ '"example-1": {
                       "summary": "POST /decision-definition/key/aKey/evaluate",
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
                       "description": "Response for POST `/decision-definition/key/aKey/evaluate`",
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