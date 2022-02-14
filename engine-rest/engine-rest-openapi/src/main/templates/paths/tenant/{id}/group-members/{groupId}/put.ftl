<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/tenant/group-members/put/index.html -->
{
  <@lib.endpointInfo
      id = "createGroupMembership"
      tag = "Tenant"
      summary = "Create Tenant Group Membership"
      desc = "Creates a membership between a tenant and a group."
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
          name = "groupId"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the group."
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
        desc = "In case an internal error occurs. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}

</#macro>