<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/user-members/delete/index.html -->
{
  <@lib.endpointInfo
      id = "deleteUserMembership"
      tag = "Tenant"
      summary = "Delete a Tenant User Membership"
      desc = "Deletes a membership between a tenant and an user."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the tenant."
      />

      <@lib.parameter
          name = "userId"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the user."
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
        code = "500"
        dto = "ExceptionDto"
        desc = "In case an error occurs. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>