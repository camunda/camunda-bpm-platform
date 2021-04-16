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
        dto = "TaskDto"
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
                         "tenantId":"aTenantId"
                       }
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