<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/get-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getJobDefinitionsCount"
      tag = "Job Definition"
      summary = "Get Job Definition Count"
      desc = "Queries for the number of job definitions that fulfill given parameters.
              Takes the same parameters as the
              [Get Job Definitions](${docsUrl}/reference/rest/job-definition/get-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/job-definition-query-params.ftl" >
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
                       "summary": "GET `/job-definition/count?activityIdIn=ServiceTask1,ServiceTask2`",
                       "description": "GET `/job-definition/count?activityIdIn=ServiceTask1,ServiceTask2`",
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