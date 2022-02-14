<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/options/index.html -->
{
  <@lib.endpointInfo
      id = "availableTenantInstanceOperations"
      tag = "Tenant"
      summary = "Tenant Resource Options"
      desc = "The `/tenant` resource supports two custom OPTIONS requests, one for the resource as such and this one for
              individual tenant instances. The OPTIONS request allows checking for the set of available operations that
              the currently authenticated user can perform on the `/tenant/{id}` resource. If the user can perform an
              operation or not may depend on various things, including the users authorizations to interact with this
              resource and the internal configuration of the process engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the tenant"
          last = true
      />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "OPTIONS `/tenant/tenantOne`",
                       "value": {
                         "links": [
                           {
                             "method": "GET",
                             "href": "http://localhost:8080/engine-rest/tenant/tenantOne",
                             "rel": "self"
                           },
                           {
                             "method": "DELETE",
                             "href": "http://localhost:8080/engine-rest/tenant/tenantOne",
                             "rel": "delete"
                           },
                           {
                             "method": "PUT",
                             "href": "http://localhost:8080/engine-rest/tenant/tenantOne",
                             "rel": "update"
                           }
                         ]
                       }
                      }']
        last = true
    />

  }

}
</#macro>