<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/post-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "queryJobsCount"
      tag = "Job"
      summary = "Get Job Count (POST)"
      desc = "Queries for jobs that fulfill given parameters. This method takes the same message
              body as the [Get Jobs POST](${docsUrl}/reference/rest/job/post-
              query/) method and therefore it is slightly more powerful than the
              [Get Job Count](${docsUrl}/reference/rest/job/get-query-count/)
              method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "JobQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/job/count`",
                     "value": {
                       "dueDates": [
                         {
                           "operator": "gt",
                           "value": "2012-07-17T17:00:00.000+0200"
                         },
                         {
                           "operator": "lt",
                           "value": "2012-07-17T18:00:00.000+0200"
                         }
                       ],
                       "createTimes": [
                         {
                           "operator": "gt",
                           "value": "2012-05-05T10:00:00.000+0200"
                         },
                         {
                           "operator": "lt",
                           "value": "2012-07-16T15:00:00.000+0200"
                         }
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
                       "description": "POST `/job/count`",
                       "value": {
                         "count": 2
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example, if an invalid operator
                for due date comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>