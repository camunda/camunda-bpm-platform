<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricProcessInstancesCount"
      tag = "Historic Process Instance"
      summary = "Get List Count (POST)"
      desc = "Queries for the number of historic process instances that fulfill the given parameters.
              This method takes the same message body as the [Get Process Instances (POST)](${docsUrl}/reference/rest/history/process-instance/get-process-instance-query/) method and
              therefore it is slightly more powerful than the [Get Process Instance Count](${docsUrl}/reference/rest/history/process-instance/post-process-instance-query-count/) method." />

  <#assign requestMethod="POST"/>
  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricProcessInstanceQueryDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/history/process-instance/count`",
                     "value": {
                                "finishedAfter": "2013-01-01T00:00:00.000+0200",
                                "finishedBefore": "2013-04-01T23:59:59.000+0200",
                                "executedActivityAfter": "2013-03-23T13:42:44.000+0200",
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
                                ]
                              }
                   }'
                ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}
</#macro>