<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/task/get-task-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricTaskInstances"
      tag = "Historic Task Instance"
      summary = "Get Tasks (Historic)"
      desc = "Queries for historic tasks that fulfill the given parameters. The size of the result
              set can be retrieved by using the
              [Get Task Count](${docsUrl}/reference/rest/history/task/get-task-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-task-instance-query-params.ftl" >
    <@lib.parameters
        object = params
        skip = ["orQueries"]  <#-- OR Queries not avaialble in GET -->
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        array = true
        dto = "HistoricTaskInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/task?taskAssignee=anAssignee&priority=42`",
                       "description": "GET `/history/task?taskAssignee=anAssignee&priority=42`",
                       "value": [
                         {
                           "id":"anId",
                           "processDefinitionId":"aProcDefId",
                           "processInstanceId":"aProcInstId",
                           "executionId":"anExecution",
                           "caseDefinitionId":"aCaseDefId",
                           "caseInstanceId":"aCaseInstId",
                           "caseExecutionId":"aCaseExecution",
                           "activityInstanceId":"anActInstId",
                           "name":"aName",
                           "description":"aDescription",
                           "deleteReason":"aDeleteReason",
                           "owner":"anOwner",
                           "assignee":"anAssignee",
                           "startTime":"2013-01-23T13:42:42.000+0200",
                           "endTime":"2013-01-23T13:45:42.000+0200",
                           "duration":2000,
                           "taskDefinitionKey":"aTaskDefinitionKey",
                           "priority":42,
                           "due":"2013-01-23T13:49:42.000+0200",
                           "parentTaskId":"aParentId",
                           "followUp:":"2013-01-23T13:44:42.000+0200",
                           "tenantId":null,
                           "removalTime":"2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId":"aRootProcessInstanceId",
                           "taskState": "aTaskState"
                         }
                       ]
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