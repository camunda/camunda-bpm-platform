<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/put-update/index.html -->
{
  <@lib.endpointInfo
      id = "updateTenant"
      tag = "Tenant"
      summary = "Update Tenant"
      desc = "Updates a given tenant."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the tenant."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TenantDto"
      examples = ['"example-1": {
                     "summary": "PUT `/tenant/tenantOne`",
                     "value": {
                       "id": "tenantOne",
                       "name": "Tenant One"
                     }
                   }']
  />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Identity service is read-only."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "If the tenant with the requested Id cannot be found."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The tenant could not be updated due to an internal server error. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>