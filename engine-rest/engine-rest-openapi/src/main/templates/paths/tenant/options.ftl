<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/options/index.html -->
{
  <@lib.endpointInfo
      id = "availableTenantResourceOperations"
      tag = "Tenant"
      summary = "Tenant Resource Options"
      desc = "The `/tenant` resource supports two custom OPTIONS requests, this one for the resource
              as such and one for individual tenant instances. The OPTIONS request
              allows checking for the set of available operations that the currently
              authenticated user can perform on the `/tenant` resource. If the user
              can perform an operation or not may depend on various things,
              including the users authorizations to interact with this resource and
              the internal configuration of the process engine."
  />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "OPTIONS `/tenant`",
                       "value": {
                         "links": [
                           {
                             "method": "GET",
                             "href": "http://localhost:8080/engine-rest/tenant",
                             "rel": "list"
                           },
                           {
                             "method": "GET",
                             "href": "http://localhost:8080/engine-rest/tenant/count",
                             "rel": "count"
                           },
                           {
                             "method": "POST",
                             "href": "http://localhost:8080/engine-rest/tenant/create",
                             "rel": "create"
                           }
                         ]
                       }
                     }']
        last = true
    />

  }

}
</#macro>