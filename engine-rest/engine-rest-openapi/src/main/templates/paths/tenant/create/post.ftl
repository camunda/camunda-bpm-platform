<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/post-create/index.html -->
{
  <@lib.endpointInfo
      id = "createTenant"
      tag = "Tenant"
      summary = "Create Tenant"
      desc = "Create a new tenant."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TenantDto"
      examples = ['"example-1": {
                     "summary": "POST `/tenant/create`",
                     "value": {
                       "id": "tenantOne",
                       "name": "Tenant One"
                     }
                   }']
  />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Identity service is read-only."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The tenant could not be created due to an internal server error. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>