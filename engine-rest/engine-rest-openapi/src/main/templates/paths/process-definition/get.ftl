<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getProcessDefinitions"
      tag = "Process Definition"
      summary = "Get List"
      desc = "Queries for process definitions that fulfill given parameters. Parameters may be the properties of 
              process definitions, such as the name, key or version. The size of the result set can be retrieved
              by using the [Get Definition Count](${docsUrl}/reference/rest/process-definition/get-query-count/) method." />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/process-definition-query-params.ftl" >

    <#assign sortByValues = [ '"category"', '"key"', '"id"', '"name"', '"version"',
                              '"deploymentId"', '"deployTime"', '"tenantId "', '"versionTag"' ] >
    <#include "/lib/commons/sort-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessDefinitionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response of GET `/process-definition?keyLike=invoice&sortBy=version&sortOrder=asc`",
                       "value": [{
                         "id": "invoice:1:c3a63aaa-2046-11e7-8f94-34f39ab71d4e",
                         "key": "invoice",
                         "category": "http://www.omg.org/spec/BPMN/20100524/MODEL",
                         "description": null,
                         "name": "Invoice Receipt",
                         "version": 1,
                         "resource": "invoice.v1.bpmn",
                         "deploymentId": "c398cd26-2046-11e7-8f94-34f39ab71d4e",
                         "diagram": null,
                         "suspended": false,
                         "tenantId": null,
                         "versionTag": null,
                         "historyTimeToLive": 5,
                         "startableInTasklist": true
                     }, {
                         "id": "invoice:2:c3e1bd16-2046-11e7-8f94-34f39ab71d4e",
                         "key": "invoice",
                         "category": "http://www.omg.org/spec/BPMN/20100524/MODEL",
                         "description": null,
                         "name": "Invoice Receipt",
                         "version": 2,
                         "resource": "invoice.v2.bpmn",
                         "deploymentId": "c3d82020-2046-11e7-8f94-34f39ab71d4e",
                         "diagram": null,
                         "suspended": false,
                         "tenantId": null,
                         "versionTag": null,
                         "historyTimeToLive": null,
                         "startableInTasklist": true
                     }]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>