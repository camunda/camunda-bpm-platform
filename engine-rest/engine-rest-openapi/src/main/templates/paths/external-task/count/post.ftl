<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "queryExternalTasksCount"
      tag = "External Task"
      summary = "Get List Count (POST)"
      desc = "Queries for the number of external tasks that fulfill given parameters. This method takes the same message
              body as the [Get External Tasks (POST)](${docsUrl}/reference/rest/external-task/post-query/) method." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExternalTaskQueryDto"
      examples = ['"example-1": {
                       "summary": "POST /external-task/count",
                       "value": {
                         "topicName": "aTopicName",
                         "withRetriesLeft": true
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>