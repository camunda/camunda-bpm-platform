<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/post-variable-instance-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricVariableInstancesCount"
      tag = "Historic Variable Instance"
      summary = "Get Variable Instance Count (POST)"
      desc = "Queries for historic variable instances that fulfill the given parameters.
              This method takes the same message body as the
              [Get Variable Instances (POST)](${docsUrl}/reference/rest/history/variable-instance/post-variable-instance-query/)
              method and therefore it is more powerful regarding variable values
              than the
              [Get Variable Instance Count](${docsUrl}/reference/rest/history/variable-instance/get-variable-instance-query-count/)
              method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricVariableInstanceQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/variable-instance/count`",
                     "value": {
                       "variableName": "someVariable",
                       "variableValue": 42
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/variable-instance/count`",
                       "description": "POST `/history/variable-instance/count`",
                       "value": {
                         "count": 1
                       }
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