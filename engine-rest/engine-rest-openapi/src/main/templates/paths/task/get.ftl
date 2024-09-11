<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTasks"
      tag = "Task"
      summary = "Get List"
      desc = "Queries for tasks that fulfill a given filter. The size of the result set can be
              retrieved by using the Get Task Count method.

              **Security Consideration:** There are several query parameters (such as
              assigneeExpression) for specifying an EL expression. These are disabled by default to
              prevent remote code execution. See the section on
              [security considerations](${docsUrl}/user-guide/process-engine/securing-custom-code/)
              for custom code in the user guide for details." />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/task-query-params.ftl" >

    <#assign sortByValues = [ '"instanceId"', '"caseInstanceId"', '"dueDate"', '"executionId"', '"caseExecutionId"',
                              '"assignee"', '"created"', '"lastUpdated"', '"description"', '"id"', '"name"', '"nameCaseInsensitive"',
                              '"priority"', '"processVariable"', '"executionVariable"', '"taskVariable"',
                              '"caseExecutionVariable"', '"caseInstanceVariable"' ] >
    <#include "/lib/commons/sort-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "TaskWithAttachmentAndCommentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response 1",
                       "description": "Response for GET `/task?assignee=anAssignee&delegationState=RESOLVED&maxPriority=50`",
                       "value": [
                         {
                           "id":"anId",
                           "name":"aName",
                           "assignee":"anAssignee",
                           "created":"2013-01-23T13:42:42.657+0200",
                           "due":"2013-01-23T13:49:42.323+0200",
                           "followUp:":"2013-01-23T13:44:42.987+0200",
                           "lastUpdated:":"2013-01-23T13:44:42.987+0200",
                           "delegationState":"RESOLVED",
                           "description":"aDescription",
                           "executionId":"anExecution",
                           "owner":"anOwner",
                           "parentTaskId":"aParentId",
                           "priority":42,
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
                           "tenantId": "aTenantId",
                           "taskState": "aTaskState"
                         }
                       ]
                     }',
        '"example-2": {
                       "summary": "Status 200 response 2",
                       "description": "Response for GET `/task?assignee=anAssignee&withCommentAttachmentInfo=true`",
                       "value": [
                         {
                           "id":"anId",
                           "name":"aName",
                           "assignee":"anAssignee",
                           "created":"2013-01-23T13:42:42.657+0200",
                           "due":"2013-01-23T13:49:42.323+0200",
                           "followUp:":"2013-01-23T13:44:42.987+0200",
                           "lastUpdated:":"2013-01-23T13:44:42.987+0200",
                           "delegationState":"RESOLVED",
                           "description":"aDescription",
                           "executionId":"anExecution",
                           "owner":"anOwner",
                           "parentTaskId":"aParentId",
                           "priority":42,
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
                           "tenantId": "aTenantId",
                           "attachment":false,
                           "comment":false
                         }
                       ]
                     }'] />

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