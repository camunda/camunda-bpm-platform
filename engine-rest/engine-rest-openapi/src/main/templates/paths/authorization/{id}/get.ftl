<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getAuthorization"
      tag = "Authorization"
      summary = "Get Authorization"
      desc = "Retrieves an authorization by id."
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
        dto = "AuthorizationDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/authorization/anAuthorizationId`",
                       "value": {
                         "id": "anAuthorizationId",
                         "type": 0,
                         "permissions": [
                           "CREATE",
                           "READ"
                         ],
                         "userId": "*",
                         "groupId": null,
                         "resourceType": 1,
                         "resourceId": "*",
                         "removalTime": "2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "f8259e5d-ab9d-11e8-8449-e4a7a094a9d6"
                       }
                     }']
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