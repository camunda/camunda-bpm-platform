<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteGroupMember"
      tag = "Group"
      summary = "Delete a Group Member"
      desc = "Removes a member from a group." />

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
        desc = "The id of user to remove from the group." />

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
        desc = "In case an error occurs. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."/>

  }
}
</#macro>