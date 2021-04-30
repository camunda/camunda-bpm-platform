<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/post-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryJobDefinitionsCount"
      tag = "Job Definition"
      summary = "Get Job Definition Count (POST)"
      desc = "Queries for the number of job definitions that fulfill given parameters. This
              method takes the same message body as the
              [Get Job Definitions (POST)](${docsUrl}/reference/rest/job-definition/post-query/)
              method and therefore it is slightly more powerful than the
              [Get Job Definition Count](${docsUrl}/reference/rest/job-definition/get-query-count/)
              method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobDefinitionQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/job-definition/count`",
                     "value": {
                       "activityIdIn": [
                         "ServiceTask1",
                         "ServiceTask2"
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/job-definition/count`",
                       "description": "POST `/job-definition/count`",
                       "value": {
                         "count": 2
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>