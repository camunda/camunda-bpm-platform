<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/post-variable-instance-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricVariableInstances"
      tag = "Historic Variable Instance"
      summary = "Get Variable Instances (POST)"
      desc = "Queries for historic variable instances that fulfill the given parameters.
              This method is slightly more powerful than the
              [Get Variable Instances](${docsUrl}/reference/rest/history/variable-instance/get-variable-instance-query/)
              method because it allows filtering by variable values of the different
              types `String`, `Number` or `Boolean`."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/pagination-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/deserialize-values-parameter.ftl">

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricVariableInstanceQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/variable-instance`",
                     "value": {
                       "variableName": "someVariable",
                       "variableValue": 42,
                       "sorting": [
                         {
                           "sortBy": "variableName",
                           "sortOrder": "asc"
                         },
                         {
                           "sortBy": "instanceId",
                           "sortOrder": "desc"
                         }
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricVariableInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/variable-instance`",
                       "description": "POST `/history/variable-instance`",
                       "value": [
                         {
                           "id": "someId",
                           "name": "someVariable",
                           "type": "Integer",
                           "variableType": "integer",
                           "value": 5,
                           "valueInfo": {},
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processInstanceId": "aProcInstId",
                           "executionId": "aExecutionId",
                           "activityInstanceId": "aActivityInstId",
                           "caseDefinitionKey": null,
                           "caseDefinitionId": null,
                           "caseInstanceId": null,
                           "caseExecutionId": null,
                           "taskId": null,
                           "tenantId": null,
                           "errorMessage": null,
                           "state": "CREATED",
                           "createTime": "2017-02-10T14:33:19.000+0200",
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
