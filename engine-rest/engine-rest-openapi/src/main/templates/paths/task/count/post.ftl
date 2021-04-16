<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "queryTasksCount"
      tag = "Task"
      summary = "Get List Count (POST)"
      desc = "Retrieves the number of tasks that fulfill the given filter. Corresponds to the size
              of the result set of the [Get Tasks (POST)](${docsUrl}/reference/rest/task/post-query/)
              method and takes the same parameters.

              **Security Consideration**:
              There are several parameters (such as `assigneeExpression`) for specifying an EL
              expression. These are disabled by default to prevent remote code execution. See the
              section on
              [security considerations for custom code](${docsUrl}/user-guide/process-engine/securing-custom-code/)
              in the user guide for details." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TaskQueryDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/task` Request Body 1",
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
                       "processInstanceBusinessKeyIn": "aBusinessKey,anotherBusinessKey",
                       "assigneeIn": "anAssignee,anotherAssignee",
                       "priority": 10,
                       "sorting": [
                         {
                           "sortBy": "dueDate",
                           "sortOrder": "asc"
                         },
                         {
                           "sortBy": "processVariable",
                           "sortOrder": "desc",
                           "parameters": {
                             "variable": "orderId",
                             "type": "String"
                           }
                         }
                       ]
                     }
                  }',
                  '"example-2": {
                     "summary": "POST `/task` Request Body 2",
                     "description": "Logical query: assignee = \\"John Munda\\" AND (name = \\"Approve Invoice\\" OR priority = 5) AND (suspended = false OR taskDefinitionKey = \\"approveInvoice\\")",
                     "value": {
                       "assignee": "John Munda",
                       "orQueries": [
                         {
                           "name": "Approve Invoice",
                           "priority": 5
                         },
                         {
                           "suspended": false,
                           "taskDefinitionKey": "approveInvoice"
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