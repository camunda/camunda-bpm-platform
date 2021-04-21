<#macro endpoint_macro docsUrl="">

    <#-- NOTE: Any changes made to this file should be mirrored to the
         ../options.ftl file as well -->
{
  <@lib.endpointInfo
      id = "availableOperationsAuthorizationInstance"
      tag = "Authorization"
      summary = "Authorization Resource Options"
      desc = "The `/authorization` resource supports two custom OPTIONS requests, one for the
              resource as such and one for individual authorization instances. The
              OPTIONS request allows you to check for the set of available
              operations that the currently authenticated user can perform on the
              `/authorization` resource. Whether the user can perform an operation
              or not may depend on various factors, including the users
              authorizations to interact with this resource and the internal
              configuration of the process engine."
  />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the authorization to be retrieved."
        last = true
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "OPTIONS `/authorization/anAuthorizationId`"
                     }']
        last = true
    />

  }

}
</#macro>