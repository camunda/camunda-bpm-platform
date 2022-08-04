<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-execute-list/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "executeFilterList"
      tag = "Filter"
      summary = "Execute Filter List"
      desc = "Executes the saved query of the filter by id and returns the result list."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to execute."
      />

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        flatType = "object"
        array = true
        desc = "Request successful. A JSON array containing JSON objects corresponding to the matching entity interface in the engine. This depends on the saved query in the filter. Therefore it is not possible to specify a generic result format, i.e., if the resource type of the filter is Task the result will correspond to the Task interface in the engine."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/filter/aTaskFilterId/list/?firstResult=0&maxResults=2`. **Note**: The examples show the result of a task filter. So the response corresponds to a task, but for other filters the response format will differ.",
                       "value": [
                         {
                           "assignee": "jonny1",
                           "caseDefinitionId": null,
                           "caseExecutionId": null,
                           "caseInstanceId": null,
                           "created": "2014-09-15T15:45:48.000+0200",
                           "delegationState": null,
                           "description": null,
                           "due": null,
                           "executionId": "aExecutionId",
                           "followUp": null,
                           "formKey": null,
                           "id": "aTaskId",
                           "lastUpdated": "2014-09-15T15:45:48.000+0200",
                           "name": "Task 2",
                           "owner": null,
                           "parentTaskId": null,
                           "priority": 50,
                           "processDefinitionId": "aProcessId",
                           "processInstanceId": "aProcessInstanceId",
                           "suspended": false,
                           "taskDefinitionKey": "aTaskKey"
                         },
                         {
                           "assignee": "demo",
                           "caseDefinitionId": null,
                           "caseExecutionId": null,
                           "caseInstanceId": null,
                           "created": "2014-09-15T10:42:18.000+0200",
                           "delegationState": null,
                           "description": null,
                           "due": null,
                           "executionId": "anotherExecutionId",
                           "followUp": null,
                           "formKey": null,
                           "id": "anotherTaskId",
                           "lastUpdated": "2014-09-15T15:45:48.000+0200",
                           "name": "Task 2",
                           "owner": null,
                           "parentTaskId": null,
                           "priority": 50,
                           "processDefinitionId": "anotherProcessId",
                           "processInstanceId": "anotherProcessInstanceId",
                           "suspended": false,
                           "taskDefinitionKey": "anotherTaskKey"
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "
                The authenticated user is unauthorized to read this filter.
                      See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "
                Filter with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>