<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/user/put-update-profile/index.html -->
{
  <@lib.endpointInfo
      id = "updateProfile"
      tag = "User"
      summary = "Update User Profile"
      desc = "Updates the profile information of an already existing user."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the user."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "UserProfileDto"
      examples = ['"example-1": {
                     "summary": "PUT `/user/jonny1/profile`",
                     "value": {
                       "id": "jonny1",
                       "firstName": "John",
                       "lastName": "Doe",
                       "email": "aNewEmailAddress"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Identity service is read-only (Cannot modify users / groups / memberships)."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "If the user with the requested Id cannot be found."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The user could not be updated due to an internal server error. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>