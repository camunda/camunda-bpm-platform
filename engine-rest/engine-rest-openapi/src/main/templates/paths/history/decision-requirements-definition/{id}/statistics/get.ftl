<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-requirements-definition/get-statistics/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionStatistics"
      tag = "Historic Decision Requirements Definition"
      summary = "Get DRD Statistics"
      desc = "Retrieves evaluation statistics of a given decision requirements definition."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the decision requirements definition."
      />

      <@lib.parameter
          name = "decisionInstanceId"
          location = "query"
          type = "string"
          desc = "Restrict query results to be based only on specific evaluation
                  instance of a given decision requirements definition."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricDecisionInstanceStatisticsDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/history/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c/statistics`",
                       "value": [
                         {
                           "decisionDefinitionKey": "dish-decision",
                           "evaluations": 1
                         }
                       ]
                     }',
                     '"example-2": {
                       "summary": "request with decisionInstanceId",
                       "description": "GET `/history/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c/statistics?decisionInstanceId=17`",
                       "value": [
                         {
                           "decisionDefinitionKey": "dish-decision",
                           "evaluations": 1
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
