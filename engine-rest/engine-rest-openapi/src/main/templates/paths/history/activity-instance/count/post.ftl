<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricActivityInstancesCount"
      tag = "Historic Activity Instance"
      summary = "Get List Count (POST)"
      desc = "Queries for the number of historic activity instances that fulfill the given parameters." />

  <#assign requestMethod="POST"/>
  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricActivityInstanceQueryDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/history/activity-instance/count`",
                     "value": {
                       "activityType": "userTask"
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