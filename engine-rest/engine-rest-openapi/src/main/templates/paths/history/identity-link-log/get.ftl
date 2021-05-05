<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/identity-links/get-identity-link-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricIdentityLinks"
      tag = "Historic Identity Link Log"
      summary = "Get Identity Link Logs"
      desc = "Queries for historic identity link logs that fulfill given parameters.
              The size of the result set can be retrieved by using the
              [Get Identity-Link-Log Count](${docsUrl}/reference/rest/history/identity-links/get-identity-link-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/history-identity-link.ftl" >
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
        dto = "HistoricIdentityLinkLogDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/identity-link-log?taskId=aTaskId`",
                       "description": "GET `/history/identity-link-log?taskId=aTaskId`",
                       "value": [
                         {
                           "id": "1",
                           "time": "2014-03-01T08:00:00.000+0200",
                           "type": "candidate",
                           "userId": "aUserId",
                           "groupId": "aGroupId",
                           "taskId": "aTaskId",
                           "processDefinitionId": "12",
                           "operationType": "add",
                           "assignerId": "aAssignerId",
                           "processDefinitionKey": "oneTaskProcess",
                           "tenantId": "tenant1",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         },
                         {
                           "id": "2",
                           "time": "2014-03-05T10:00:00.000+0200",
                           "type": "candidate",
                           "userId": "aUserId",
                           "groupId": "aGroupId",
                           "taskId": "aTaskId",
                           "processDefinitionId": "12",
                           "operationType": "delete",
                           "assignerId": "aAssignerId",
                           "processDefinitionKey": "oneTaskProcess",
                           "tenantId": "tenant1",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
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
