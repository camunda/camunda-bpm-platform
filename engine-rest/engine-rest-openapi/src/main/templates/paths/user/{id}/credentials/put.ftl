<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateCredentials"
      tag = "User"
      summary = "Update Credentials"
      desc = "Updates a user's credentials (password)" />

  "parameters" : [

     <@lib.parameter
         name = "id"
         location = "path"
         type = "string"
         required = true
         desc = "The id of the user to be updated." />

     <@lib.parameter
         name = "password"
         location = "query"
         type = "string"
         required = true
         desc = "The users new password." />

     <@lib.parameter
         name = "authenticatedUserPassword"
         location = "query"
         type = "string"
         required = true
         last = true
         desc = "The password of the authenticated user who changes the password of the user
                 (i.e., the user with passed id as path parameter)." />
  ],

  <@lib.requestBody
        mediaType = "application/json"
        dto = "UserCredentialsDto"
        examples = ['"example-1": {
                             "summary": "PUT /user/jonny1/credentials",
                             "value": {
                               "password": "s3cr3t",
                               "authenticatedUserPassword" : "demo"
                             }
                           }'] />

  "responses" : {

    <@lib.response
        code = "200"
        desc = "Request successful." />

    <@lib.response
        code = "403"
        mediaType = "application/json"
        desc = "Identity service is read-only (Cannot modify users / groups / memberships)." />

    <@lib.response
        code = "400"
        mediaType = "application/json"
        desc = "The authenticated user password does not match" />

    <@lib.response
        code = "404"
        mediaType = "application/json"
        desc = "If the corresponding user cannot be found" />

    <@lib.response
       code = "500"
       mediaType = "application/json"
       dto = "ExceptionDto"
       last = true
       desc = "The user could not be updated due to an internal server error. See the
               [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
               for the error response format." />
  }
}
</#macro>