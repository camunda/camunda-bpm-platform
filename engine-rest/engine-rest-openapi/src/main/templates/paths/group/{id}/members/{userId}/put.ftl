<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "createGroupMember"
      tag = "Group"
      summary = "Create Group Member"
      desc = "Adds a member to a group." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the group." />

    <@lib.parameter
        name = "userId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of user to add to the group." />

  ],

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Identity service is read-only (Cannot modify users / groups / memberships)."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "In case an internal error occurs. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."/>

  }
}
</#macro>