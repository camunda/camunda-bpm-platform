<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/delete/index.html -->
{
  <@lib.endpointInfo
      id = "deleteTenant"
      tag = "Tenant"
      summary = "Delete Tenant"
      desc = "Deletes a tenant by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the tenant to be deleted."
          last = true
      />

  ],

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
        code = "404"
        dto = "ExceptionDto"
        desc = "Tenant cannot be found. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>