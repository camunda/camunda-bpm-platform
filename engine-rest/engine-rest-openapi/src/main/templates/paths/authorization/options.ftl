<#macro endpoint_macro docsUrl="">

    <#-- NOTE: Any changes made to this file should be mirrored to the
         ./{id}/options.ftl file as well -->
{
  <@lib.endpointInfo
      id = "availableOperationsAuthorization"
      tag = "Authorization"
      summary = "Authorization Resource Options"
      desc = "The OPTIONS request allows you to check for the set of available operations that the currently
              authenticated user can perform on the `/authorization` resource. Whether the user can perform an operation
              or not may depend on various factors, including the users authorizations to interact with this
              resource and the internal configuration of the process engine."
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "OPTIONS `/authorization`",
                       "value": {
                         "links": [
                           {
                             "method":"GET",
                             "href":"http://localhost:8080/engine-rest/authorization",
                             "rel":"list"
                           },
                           {
                             "method":"GET",
                             "href":"http://localhost:8080/engine-rest/authorization/count",
                             "rel":"count"
                           },
                           {
                             "method":"POST",
                             "href":"http://localhost:8080/engine-rest/authorization/create",
                             "rel":"create"
                           }
                         ]
                       }
                     }']
        last = true
    />

  }

}
</#macro>