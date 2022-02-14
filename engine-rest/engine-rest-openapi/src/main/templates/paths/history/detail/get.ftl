<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricDetails"
      tag = "Historic Detail"
      summary = "Get Historic Details"
      desc = "Queries for historic details that fulfill the given parameters.
              The size of the result set can be retrieved by using the
              [Get Historic Detail Count](${docsUrl}/reference/rest/history/detail/get-detail-query-count/) method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-detail-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#include "/lib/commons/pagination-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/deserialize-values-parameter.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricDetailDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/detail?processInstanceId=3cd597b7-001a-11e7-8c6b-34f39ab71d4e`",
                       "description": "GET `/history/detail?processInstanceId=3cd597b7-001a-11e7-8c6b-34f39ab71d4e`",
                       "value": [
                         {
                           "type": "variableUpdate",
                           "id": "3cd79390-001a-11e7-8c6b-34f39ab71d4e",
                           "processDefinitionKey": "invoice",
                           "processDefinitionId": "invoice:1:3c59899b-001a-11e7-8c6b-34f39ab71d4e",
                           "processInstanceId": "3cd597b7-001a-11e7-8c6b-34f39ab71d4e",
                           "activityInstanceId": "StartEvent_1:3cd7456e-001a-11e7-8c6b-34f39ab71d4e",
                           "executionId": "3cd597b7-001a-11e7-8c6b-34f39ab71d4e",
                           "caseDefinitionKey": null,
                           "caseDefinitionId": null,
                           "caseInstanceId": null,
                           "caseExecutionId": null,
                           "taskId": null,
                           "tenantId": null,
                           "userOperationId": "3cd76c7f-001a-11e7-8c6b-34f39ab71d4e",
                           "time": "2017-03-03T15:03:54.000+0200",
                           "variableName": "amount",
                           "variableInstanceId": "3cd65b08-001a-11e7-8c6b-34f39ab71d4e",
                           "variableType": "Double",
                           "value": 30.0,
                           "valueInfo": {},
                           "revision": 0,
                           "errorMessage": null,
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId",
                           "initial": true
                         },
                         {
                           "type": "variableUpdate",
                           "id": "3cd79392-001a-11e7-8c6b-34f39ab71d4e",
                           "processDefinitionKey": "invoice",
                           "processDefinitionId": "invoice:1:3c59899b-001a-11e7-8c6b-34f39ab71d4e",
                           "processInstanceId": "3cd597b7-001a-11e7-8c6b-34f39ab71d4e",
                           "activityInstanceId": "StartEvent_1:3cd7456e-001a-11e7-8c6b-34f39ab71d4e",
                           "executionId": "3cd597b7-001a-11e7-8c6b-34f39ab71d4e",
                           "caseDefinitionKey": null,
                           "caseDefinitionId": null,
                           "caseInstanceId": null,
                           "caseExecutionId": null,
                           "taskId": null,
                           "tenantId": null,
                           "userOperationId": "3cd76c7f-001a-11e7-8c6b-34f39ab71d4e",
                           "time": "2017-03-03T15:03:54.000+0200",
                           "variableName": "invoiceDocument",
                           "variableInstanceId": "3cd65b0a-001a-11e7-8c6b-34f39ab71d4e",
                           "variableType": "File",
                           "value": null,
                           "valueInfo": {
                             "mimeType": "application/pdf",
                             "filename": "invoice.pdf"
                           },
                           "revision": 0,
                           "errorMessage": null,
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId",
                           "initial": true
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