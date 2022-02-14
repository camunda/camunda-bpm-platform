<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "queryExecutionsCount"
      tag = "Execution"
      summary = "Get Execution Count (POST)"
      desc = "Queries for the number of executions that fulfill given parameters. This method
              takes the same message body as the [Get Executions
              POST](${docsUrl}/reference/rest/execution/post-query/) method and
              therefore it is slightly more powerful than the [Get Execution
              Count](${docsUrl}/reference/rest/execution/get-query-count/) method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExecutionQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/execution/count`",
                     "value": {
                       "variables": [
                         {
                           "name": "myVariable",
                           "operator": "eq",
                           "value": "camunda"
                         },
                         {
                           "name": "mySecondVariable",
                           "operator": "neq",
                           "value": 124
                         }
                       ],
                       "processDefinitionId": "aProcessDefinitionId"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "POST `/execution/count`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if an invalid operator
                for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>