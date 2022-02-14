<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getDeployments"
      tag = "Deployment"
      summary = "Get List"
      desc = "Queries for deployments that fulfill given parameters. Parameters may be the properties of deployments,
              such as the id or name or a range of the deployment time. The size of the result set can be retrieved by
              using the [Get Deployment count](${docsUrl}/reference/rest/deployment/get-query-count/) method." />

  "parameters" : [

    <#assign last = false >

    <#include "/lib/commons/deployment-query-params.ftl" >

    <#assign sortByValues = [ '"id"', '"name"', '"deploymentTime"', '"tenantId"' ] >
    <#include "/lib/commons/sort-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "DeploymentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/deployment?name=deploymentName`",
                       "value": [
                         {
                           "id": "someId",
                           "name": "deploymentName",
                           "source": "process application",
                           "tenantId": null,
                           "deploymentTime": "2013-04-23T13:42:43.000+0200"
                         }
                       ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy`, or if an invalid operator for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>