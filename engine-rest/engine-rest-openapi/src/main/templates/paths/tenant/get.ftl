<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/get-query/index.html -->
{
  <@lib.endpointInfo
      id = "queryTenants"
      tag = "Tenant"
      summary = "Get Tenants"
      desc = "Query for a list of tenants using a list of parameters. The size of the result set
              can be retrieved by using the [Get Tenant
              Count](${docsUrl}/reference/rest/tenant/get-query-count/) method."
  />

  "parameters" : [

    <#include "/lib/commons/tenant-query-params.ftl" >

    <#assign last = false >

    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >

    <@lib.parameters
        object = params
        last = true
    />
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "TenantDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/tenant?name=tenantOne`",
                       "value": [
                         {
                           "id": "tenantOne",
                           "name": "Tenant One"
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy` is specified. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>