<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/get-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "getJobsCount"
      tag = "Job"
      summary = "Get Job Count"
      desc = "Queries for the number of jobs that fulfill given parameters.
              Takes the same parameters as the [Get
              Jobs](${docsUrl}/reference/rest/job/get-query/) method."
  />

  "parameters" : [

    <#assign last = true >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/job-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/job/count?dueDates=gt_2012-07-17T17:00:00.000+0200,lt_2012-07-17T18:00:00.000+0200&createTimes=gt_2012-05-05T10:00:00.000+0200,lt_2012-07-16T15:00:00.000+0200`",
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