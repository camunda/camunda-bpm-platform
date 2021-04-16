<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/get-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "getTenantCount"
      tag = "Tenant"
      summary = "Get Tenant Count"
      desc = "Query for tenants using a list of parameters and retrieves the count."
  />

  "parameters" : [

    <#include "/lib/commons/tenant-query-params.ftl" >

    <@lib.parameters
        object = params
        last = true
    />
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/tenant/count?name=tenantOne`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>