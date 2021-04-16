<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "unlockUser"
      tag = "User"
      summary = "Unlock User"
      desc = "Unlocks a user by id." />

  "parameters" : [

     <@lib.parameter
         name = "id"
         location = "path"
         type = "string"
         required = true
         last = true
         desc = "The id of the user to be unlocked." />
  ],

  "responses" : {

    <@lib.response
        code = "204"
        mediaType = "application/json"
        desc = "Request successful." />

    <@lib.response
        code = "403"
        mediaType = "application/json"
        desc = "The user who performed the operation is not a Camunda admin user." />

    <@lib.response
       code = "404"
       mediaType = "application/json"
       dto = "ExceptionDto"
       last = true
       desc = "User cannot be found. See the
               [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
               for the error response format." />
  }
}
</#macro>