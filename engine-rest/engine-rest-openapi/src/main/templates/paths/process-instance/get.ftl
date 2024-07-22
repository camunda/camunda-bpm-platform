<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getProcessInstances"
      tag = "Process Instance"
      summary = "Get List"
      desc = "Queries for process instances that fulfill given parameters.
              Parameters may be static as well as dynamic runtime properties of process instances.
              The size of the result set can be retrieved by using the Get Instance Count method." />

  "parameters" : [

    <#assign last = false >
    <#assign sortByValues = ['"instanceId"', '"definitionKey"', '"definitionId"', '"tenantId"', '"businessKey"']>
    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/process-instance-query-params.ftl" >

  ],
  "responses" : {
    <@lib.response
        code = "200"
        dto = "ProcessInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/process-instance?variables=myVariable_eq_camunda,mySecondVariable_neq_aBadValue`",
                       "value": [
                         {
                           "links": [],
                           "id": "anId",
                           "definitionId": "aProcDefId",
                           "definitionKey": "aProcDefKey",
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
