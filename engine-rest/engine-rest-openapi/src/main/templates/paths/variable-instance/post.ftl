<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/post-query/index.html -->
{
  <@lib.endpointInfo
      id = "queryVariableInstances"
      tag = "Variable Instance"
      summary = "Get Variable Instances (POST)"
      desc = "Query for variable instances that fulfill given parameters through a JSON object.
              This method is slightly more powerful than the
              [Get Variable Instances](${docsUrl}/reference/rest/variable-
              instance/get-query/) method because it allows filtering by multiple
              variable instances of types `String`, `Number` or `Boolean`."
  />

  "parameters" : [

    <#assign requestMethod="POST"/>
    <#assign last = false >
    <#include "/lib/commons/pagination-params.ftl" >
    <#assign last = true >
    <#include "/lib/commons/deserialize-values-parameter.ftl">

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "VariableInstanceQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/variable-instance`",
                     "value": {
                       "variableValues": [
                         {
                           "name": "amount",
                           "operator": "gteq",
                           "value": 5
                         },
                         {
                           "name": "amount",
                           "operator": "lteq",
                           "value": 200
                         }
                       ],
                       "processInstanceIdIn": [
                         "aProcessInstanceId",
                         "anotherProcessInstanceId"
                       ],
                       "sorting": [
                         {
                           "sortBy": "variableType",
                           "sortOrder": "asc"
                         }
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "VariableInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "POST `/variable-instance`",
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