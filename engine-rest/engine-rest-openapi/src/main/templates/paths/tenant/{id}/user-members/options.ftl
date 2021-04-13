<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/user-members/options/index.html -->
{
  <@lib.endpointInfo
      id = "availableTenantUserMembersOperations"
      tag = "Tenant"
      summary = "Tenant User Membership Resource Options"
      desc = "The OPTIONS request allows checking for the set of available operations that the
              currently authenticated user can perform on the resource. If the user
              can perform an operation or not may depend on various things,
              including the users authorizations to interact with this resource and
              the internal configuration of the process engine."
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
                       "description": "OPTIONS `/tenant/tenantOne/user-members`",
                       "value": {
                         "links": [
                           {
                             "method": "DELETE",
                             "href": "http://localhost:8080/engine-rest/tenant/tenantOne/user-members",
                             "rel": "delete"
                           },
                           {
                             "method": "PUT",
                             "href": "http://localhost:8080/engine-rest/tenant/tenantOne/user-members",
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