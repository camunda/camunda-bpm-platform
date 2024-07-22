<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "queryProcessInstances"
      tag = "Process Instance"
      summary = "Get List (POST)"
      desc = "Queries for process instances that fulfill given parameters through a JSON object.
              This method is slightly more powerful than the Get Instances method because
              it allows filtering by multiple process variables of types `string`, `number` or `boolean`." />

  "parameters" : [
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >
   ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceQueryDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/process-instance` Request Body 1",
                     "value": {
                         "variables":
                         [{
                             "name": "myVariable",
                             "operator": "eq",
                             "value": "camunda"
                           }, {
                             "name": "mySecondVariable",
                             "operator": "neq",
                             "value": 124
                           }
                         ],
                         "processDefinitionId": "aProcessDefinitionId",
                         "sorting":
                         [{
                             "sortBy": "definitionKey",
                             "sortOrder": "asc"
                           }, {
                             "sortBy": "instanceId",
                             "sortOrder": "desc"
                           }
                         ]
                       }
                   }'
                ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
                       "value": [
                         {
                           "links": [],
                           "id": "anId",
                           "definitionId": "aProcessDefinitionId",
                           "definitionKey": "aProcessDefinitionKey",
                           "businessKey": "aKey",
                           "caseInstanceId": "aCaseInstanceId",
                           "ended": false,
                           "suspended": false,
                           "tenantId": null
                         }
                       ]
                   }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid,
                for example if a sortOrder parameter is supplied, but no sortBy, or if an invalid operator for variable comparison is used."/>

  }
}

</#macro>
