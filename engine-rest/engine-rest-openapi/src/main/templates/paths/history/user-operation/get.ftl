<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/user-operation-log/get-user-operation-log-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryUserOperationEntries"
      tag = "Historic User Operation Log"
      summary = "Get User Operation Log (Historic)"
      desc = "Queries for user operation log entries that fulfill the given parameters.
              The size of the result set can be retrieved by using the
              [Get User Operation Log Count](${docsUrl}/reference/rest/history/user-operation-log/get-user-operation-log-query-count/)
              method.

              Note that the properties of operation log entries are interpreted as
              restrictions on the entities they apply to. That means, if a single
              process instance is updated, the field `processInstanceId` is
              populated. If a single operation updates all process instances of the
              same process definition, the field `processInstanceId` is `null` (a
              `null` restriction is viewed as a wildcard, i.e., matches a process
              instance with any id) and the field `processDefinitionId` is
              populated. This way, which entities were changed by a user operation
              can easily be reconstructed."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/user-operation-log-query-params.ftl">
    <@lib.parameters
        object = params
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
        dto = "UserOperationLogEntryDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Gets an operation that updates a single task.",
                       "description": "GET `/history/user-operation?operationType=Claim&userId=demo&sortBy=timestamp&sortOrder=asc`",
                       "value": [
                         {
                           "id":"anUserOperationLogEntryId",
                           "deploymentId":"aDeploymentId",
                           "processDefinitionId":"aProcessDefinitionId",
                           "processDefinitionKey":null,
                           "processInstanceId":"aProcessInstanceId",
                           "executionId":"anExecutionId",
                           "taskId":"aTaskId",
                           "jobId":"aJobId",
                           "jobDefinitionId":"aJobDefinitionId",
                           "userId":"demo",
                           "timestamp":"2014-02-25T14:58:37.000+0200",
                           "operationId":"anOperationId",
                           "operationType":"Claim",
                           "entityType":"Task",
                           "property":"assignee",
                           "orgValue":null,
                           "newValue":"demo",
                           "removalTime":"2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId":"aRootProcessInstanceId",
                           "category":"TaskWorker",
                           "annotation":"anAnnotation"
                         }
                       ]
                     }',
                     '"example-2": {
                       "summary": "Gets an operation that updates a multiple process instances with the same key.",
                       "description": "GET `/history/user-operation?operationType=Suspend&userId=demo`",
                       "value": [
                         {
                           "id":"anUserOperationLogEntryId",
                           "deploymentId":"aDeploymentId",
                           "processDefinitionId":"aProcessDefinitionId",
                           "processDefinitionKey":"aProcessDefinitionKey",
                           "processInstanceId":null,
                           "executionId":null,
                           "taskId":null,
                           "jobId":null,
                           "jobDefinitionId":null,
                           "userId":"demo",
                           "timestamp":"2014-02-25T14:58:37.000+0200",
                           "operationId":"anOperationId",
                           "operationType":"Suspend",
                           "entityType":"ProcessInstance",
                           "property":"suspensionState",
                           "orgValue":null,
                           "newValue":"suspended",
                           "removalTime":"2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId":"aRootProcessInstanceId",
                           "category":"Operator",
                           "annotation":"anAnnotation"
                         }
                       ]
                     }'
        ]
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