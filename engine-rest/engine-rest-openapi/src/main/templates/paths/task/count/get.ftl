<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTasksCount"
      tag = "Task"
      summary = "Get List Count"
      desc = "Retrieves the number of tasks that fulfill a provided filter. Corresponds to the size
              of the result set when using the [Get Tasks](${docsUrl}/reference/rest/task/) method.

              **Security Consideration:** There are several query parameters (such as
              assigneeExpression) for specifying an EL expression. These are disabled by default to
              prevent remote code execution. See the section on
              [security considerations](${docsUrl}/user-guide/process-engine/securing-custom-code/)
              for custom code in the user guide for details." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/task-query-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
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