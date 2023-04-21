<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "isUserAuthorized"
      tag = "Authorization"
      summary = "Perform an Authorization Check"
      desc = "Performs an authorization check for the currently authenticated user."
  />

  "parameters" : [

      <@lib.parameter
          name = "permissionName"
          location = "query"
          type = "string"
          required = true
          desc = "String value representing the permission name to check for."
      />

      <@lib.parameter
          name = "resourceName"
          location = "query"
          type = "string"
          required = true
          desc = "String value for the name of the resource to check permissions for."
      />

      <@lib.parameter
          name = "resourceType"
          location = "query"
          type = "integer"
          format="int32"
          required = true
          desc = "An integer representing the resource type to check permissions for.
                  See the [User Guide](${docsUrl}/user-guide/process-engine/authorization-service/#resources)
                  for a list of integer representations of resource types."
      />

      <@lib.parameter
          name = "resourceId"
          location = "query"
          type = "string"
          desc = "The id of the resource to check permissions for. If left blank,
                  a check for global permissions on the resource is performed."
      />

      <@lib.parameter
          name = "userId"
          location = "query"
          type = "string"
          desc = "The id of the user to check permissions for. The currently authenticated
                  user must have a READ permission for the Authorization resource. If `userId` is
                  blank, a check for the currently authenticated user is performed."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "AuthorizationCheckResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/authorization/check?permissionName=READ,resourceName=USER,resourceType=1,resourceId=jonny`",
                       "value": {
                         "permissionName": "READ",
                         "resourceName": "USER",
                         "resourceId": "jonny",
                         "authorized": true
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a permission
                parameterName is not valid for the provided resourceType. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "401"
        dto = "ExceptionDto"
        desc = "The user is not authenticated. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "When a `userId` is passed and the user does not possess a READ permission for the
                Authorization resource. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Authorization with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
