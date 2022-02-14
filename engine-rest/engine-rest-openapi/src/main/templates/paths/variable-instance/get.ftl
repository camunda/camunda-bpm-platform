<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/get-query/index.html -->
{
  <@lib.endpointInfo
      id = "getVariableInstances"
      tag = "Variable Instance"
      summary = "Get Variable Instances"
      desc = "Query for variable instances that fulfill given parameters. Parameters may be the
              properties of variable instances, such as the name or type. The size
              of the result set can be retrieved by using the [Get Variable Instance
              Count](${docsUrl}/reference/rest/variable-instance/get-query-count/)
              method."
  />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/variable-instance-query-params.ftl" >
    <#assign last = false >

    <@lib.parameters
        object = params
    />

    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >
    <#assign last = true >

    <#include "/lib/commons/deserialize-values-parameter.ftl">

],

  "responses": {

    <@lib.response
        code = "200"
        dto = "VariableInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/variable-instance?processInstanceIdIn=aProcessInstanceId,anotherProcessInstanceId&variableValues=amount_gteq_5,amount_lteq_200`",
                       "value": [
                         {
                           "id": "someId",
                           "name": "amount",
                           "type": "Integer",
                           "value": 5,
                           "processDefinitionId": "aProcessDefinitionId",
                           "processInstanceId": "aProcessInstanceId",
                           "executionId": "b68b71c9-e310-11e2-beb0-f0def1557726",
                           "taskId": null,
                           "batchId": null,
                           "activityInstanceId": "Task_1:b68b71ca-e310-11e2-beb0-f0def1557726",
                           "caseExecutionId": null,
                           "caseInstanceId": null,
                           "errorMessage": null,
                           "tenantId": null
                         },
                         {
                           "id": "someOtherId",
                           "name": "amount",
                           "type": "Integer",
                           "value": 15,
                           "processDefinitionId": "aProcessDefinitionId",
                           "processInstanceId": "aProcessInstanceId",
                           "executionId": "68b71c9-e310-11e2-beb0-f0def1557726",
                           "taskId": null,
                           "batchId": null,
                           "activityInstanceId": "Task_1:b68b71ca-e310-11e2-beb0-f0def1557726",
                           "caseExecutionId": null,
                           "caseInstanceId": null,
                           "errorMessage": null,
                           "tenantId": null
                         },
                         {
                           "id": "yetAnotherId",
                           "name": "amount",
                           "type": "Integer",
                           "value": 150,
                           "processDefinitionId": "aProcessDefinitionId",
                           "processInstanceId": "anotherProcessInstanceId",
                           "executionId": "68b71c9-e310-11e2-beb0-f0def1557726",
                           "taskId": null,
                           "batchId": null,
                           "activityInstanceId": "Task_2:b68b71ca-e310-11e2-beb0-f0def1557726",
                           "caseExecutionId": null,
                           "caseInstanceId": null,
                           "errorMessage": null,
                           "tenantId": null
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`, or if an invalid operator
                for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}

</#macro>