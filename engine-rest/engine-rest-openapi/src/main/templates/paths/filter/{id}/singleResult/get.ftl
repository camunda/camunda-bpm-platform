<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-execute-single-result/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "executeFilterSingleResult"
      tag = "Filter"
      summary = "Execute Filter Single Result"
      desc = "Executes the saved query of the filter by id and returns the single result."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to execute."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        flatType = "object"
        desc = "Request successful. A JSON object corresponding to the matching entity interface in the engine. This depends on the saved query in the filter. Therefore it is not possible to specify a generic result format, i.e., if the resource type of the filter is Task the result will correspond to the Task interface in the engine."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/filter/aTaskFilterId/singleResult`. **Note**: The examples show the result of a task filter. So the response corresponds to a task, but for other filters the response format will differ.",
                       "value": {
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
                       }
                     }']
    />

    <@lib.response
        code = "204"
        desc = "Request successful, but the result was empty. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                The executed filter returned more than one single result. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
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