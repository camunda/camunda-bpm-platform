<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/task/post-task-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricTaskInstancesCount"
      tag = "Historic Task Instance"
      summary = "Get Task Count (POST)"
      desc = "Queries for the number of historic tasks that fulfill the given parameters. Takes the
              same parameters as the [Get Tasks (Historic)](${docsUrl}/reference/rest/history/task/get-task-query/)
              method. Corresponds to the size of the result set of the
              [Get Tasks (Historic) (POST)](${docsUrl}/reference/rest/history/task/post-task-query/)
              method and takes the same parameters."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricTaskInstanceQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/task/count`",
                     "value": {
                       "taskVariables": [
                         {
                           "name": "varName",
                           "value": "varValue",
                           "operator": "eq"
                         },
                         {
                           "name": "anotherVarName",
                           "value": 30,
                           "operator": "neq"
                         }
                       ],
                       "priority": 10
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/task/count`",
                       "description": "POST `/history/task/count`",
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