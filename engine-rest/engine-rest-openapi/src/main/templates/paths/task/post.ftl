<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "queryTasks"
      tag = "Task"
      summary = "Get List (POST)"
      desc = "Queries for tasks that fulfill a given filter. This method is slightly more powerful
              than the [Get Tasks](${docsUrl}/reference/rest/task/get-query/) method because it
              allows filtering by multiple process or task variables of types `String`, `Number`
              or `Boolean`. The size of the result set can be retrieved by using the
              [Get Task Count (POST)](${docsUrl}/reference/rest/task/post-query-count/) method.

              **Security Consideration**:
              There are several parameters (such as `assigneeExpression`) for specifying an EL
              expression. These are disabled by default to prevent remote code execution. See the
              section on
              [security considerations for custom code](${docsUrl}/user-guide/process-engine/securing-custom-code/)
              in the user guide for details." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

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
                       "withCommentAttachmentInfo": "true",
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
        dto = "TaskWithAttachmentAndCommentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response 1",
                       "value": [
                         {
                           "id":"anId",
                           "name":"aName",
                           "assignee":"anAssignee",
                           "created":"2013-01-23T13:42:42.453+0200",
                           "due":"2013-01-23T13:49:42.342+0200",
                           "followUp:":"2013-01-23T13:44:42.546+0200",
                           "lastUpdated:":"2013-01-23T13:44:42.546+0200",
                           "delegationState":"RESOLVED",
                           "description":"aDescription",
                           "executionId":"anExecution",
                           "owner":"anOwner",
                           "parentTaskId":"aParentId",
                           "priority":10,
                           "processDefinitionId":"aProcDefId",
                           "processInstanceId":"aProcInstId",
                           "caseDefinitionId":"aCaseDefId",
                           "caseInstanceId":"aCaseInstId",
                           "caseExecutionId":"aCaseExecution",
                           "taskDefinitionKey":"aTaskDefinitionKey",
                           "suspended": false,
                           "formKey":"aFormKey",
                           "camundaFormRef":{
                             "key": "aCamundaFormKey",
                             "binding": "version",
                             "version": 2
                           },
                           "tenantId":"aTenantId",
                           "taskState": "aTaskState"
                         }
                       ]
                    }',
                    '"example-2": {
                       "summary": "Status 200 response 2",
                       "value": [
                         {
                           "id": "349fffa8-6571-11e7-9a44-d6940f5ef88d",
                           "name": "Approve Invoice",
                           "assignee": "John Munda",
                           "created": "2017-07-10T15:10:54.670+0200",
                           "due": "2017-07-17T15:10:54.670+0200",
                           "followUp": null,
                           "lastUpdated": "2017-07-17T15:10:54.670+0200",
                           "delegationState": null,
                           "description": "Approve the invoice (or not).",
                           "executionId": "349f8a5c-6571-11e7-9a44-d6940f5ef88d",
                           "owner": null,
                           "parentTaskId": null,
                           "priority": 50,
                           "processDefinitionId": "invoice:1:2c8d8057-6571-11e7-9a44-d6940f5ef88d",
                           "processInstanceId": "349f8a5c-6571-11e7-9a44-d6940f5ef88d",
                           "taskDefinitionKey": "approveInvoice",
                           "caseExecutionId": null,
                           "caseInstanceId": null,
                           "caseDefinitionId": null,
                           "suspended": false,
                           "formKey": "embedded:app:develop/invoice-forms/approve-invoice.html",
                           "tenantId": null,
                           "taskState": "aTaskState",
                           "attachment":false,
                           "comment":false
                         }
                       ]
                     }'
                   ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`, or if an invalid operator for variable
                comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
</#macro>