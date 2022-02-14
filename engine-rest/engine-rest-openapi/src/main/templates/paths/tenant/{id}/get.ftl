<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/get/index.html -->
{
  <@lib.endpointInfo
      id = "getTenant"
      tag = "Tenant"
      summary = "Get Tenant"
      desc = "Retrieves a tenant."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the tenant to be retrieved."
          last = true
      />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "TenantDto"
        desc = "Request successful."
        examples = ['"example-1": {
                          "summary": "Status 200.",
                          "description": "GET `/tenant/tenantOne`",
                          "value": {
                            "id": "tenantOne",
                            "name": "Tenant One"
                          }
                        }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Tenant with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>