<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTask"
      tag = "Task"
      summary = "Get"
      desc = "Retrieves a task by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to be retrieved."/>

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "TaskWithAttachmentAndCommentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /task/anId Response",
                       "value": {
                         "id":"anId",
                         "name":"aName",
                         "assignee":"anAssignee",
                         "created":"2013-01-23T13:42:42.000+0200",
                         "due":"2013-01-23T13:49:42.576+0200",
                         "followUp":"2013-01-23T13:44:42.437+0200",
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
                         "tenantId":"aTenantId",
			 "taskState": "aTaskState"
                       }
                     }',
        '"example-2": {
                       "summary": "GET /task/anId?withCommentAttachmentInfo=true Response",
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
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Task with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>
