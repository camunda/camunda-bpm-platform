<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "updateAuthorization"
      tag = "Authorization"
      summary = "Update an Authorization"
      desc = "Updates an authorization by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the authorization to be updated."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "AuthorizationUpdateDto"
      examples = ['"example-1": {
                     "summary": "PUT `/authorization/anAuthorizationId`",
                     "value": {
                       "permissions": 16,
                       "userId": "*",
                       "groupId": null,
                       "resourceType": 1,
                       "resourceId": "*"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the properties in the request body are invalid, for example if
                a permission parameter is not valid for the provided resourceType.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "The authenticated user is unauthorized to update this resource. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "The authorization with the requested Id cannot be found."
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