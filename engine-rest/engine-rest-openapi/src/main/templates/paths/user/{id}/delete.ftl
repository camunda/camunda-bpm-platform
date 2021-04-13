<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteUser"
      tag = "User"
      summary = "Delete"
      desc = "Deletes a user by id." />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          last = true
          desc = "The id of the user to be deleted." />
  ],

  "responses" : {

       <@lib.response
               code = "204"
               desc = "Request successful." />

       <@lib.response
                code = "403"
                desc = "Identity service is read-only (Cannot modify users / groups / memberships)."
                />

      <@lib.response
           code = "404"
           dto = "ExceptionDto"
           last = true
           desc = "A Deployment with the provided id does not exist. See the
                   [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                   for the error response format." />
  }
}
</#macro>