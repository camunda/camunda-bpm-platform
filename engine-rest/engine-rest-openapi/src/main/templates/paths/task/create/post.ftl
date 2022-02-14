<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "createTask"
      tag = "Task"
      summary = "Create"
      desc = "Creates a new task." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TaskDto"
      examples = ['"example-1": {
                     "summary": "POST /task/create",
                     "value": {
                       "id": "aTaskId",
                       "name": "My Task",
                       "description": "This have to be done very urgent",
                       "priority": 30,
                       "assignee": "peter",
                       "owner": "mary",
                       "delegationState": "PENDING",
                       "due": "2014-08-30T10:00:00.000+0200",
                       "followUp": "2014-08-25T10:00:00.000+0200",
                       "parentTaskId": "aParentTaskId",
                       "caseInstanceId": "aCaseInstanceId",
                       "tenantId": null
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."  />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if a not valid `delegationState` is supplied. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>