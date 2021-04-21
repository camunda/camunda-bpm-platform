<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "createAuthorization"
      tag = "Authorization"
      summary = "Create a New Authorization"
      desc = "Creates a new authorization."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "AuthorizationCreateDto"
      examples = ['"example-1": {
                     "summary": "POST `/authorization/create`",
                     "value": {
                       "type": 0,
                       "permissions": [
                         "CREATE",
                         "READ"
                       ],
                       "userId": "*",
                       "groupId": null,
                       "resourceType": 1,
                       "resourceId": "*"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "AuthorizationDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/authorization/create`"
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the properties in the request body are invalid, for example if
                a permission parameter is not valid for the provided resourceType.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-
                handling) for the error response format."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "The authenticated user is unauthorized to create an instance of this resource. See
                the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The authorization could not be updated due to an internal server error. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>