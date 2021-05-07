<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/get-variable-instance-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricVariableInstances"
      tag = "Historic Variable Instance"
      summary = "Get Variable Instances"
      desc = "Queries for historic variable instances that fulfill the given parameters.
              The size of the result set can be retrieved by using the
              [Get Variable Instance Count](${docsUrl}/reference/rest/history/variable-instance/get-variable-instance-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/historic-variable-instance-query-params.ftl" >
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
        dto = "HistoricVariableInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/variable-instance?variableName=my_variable`",
                       "description": "GET `/history/variable-instance?variableName=my_variable`",
                       "value": [
                         {
                           "id": "someId",
                           "name": "my_variable",
                           "type": "String",
                           "value": "my_value",
                           "valueInfo": {},
                           "processDefinitionKey": "aVariableInstanceProcDefKey",
                           "processDefinitionId": "aVariableInstanceProcDefId",
                           "processInstanceId": "aVariableInstanceProcInstId",
                           "executionId": "aVariableInstanceExecutionId",
                           "activityInstanceId": "aVariableInstanceActivityInstId",
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
