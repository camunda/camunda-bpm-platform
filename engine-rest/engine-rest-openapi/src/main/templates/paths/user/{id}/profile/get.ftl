<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getUserProfile"
      tag = "User"
      summary = "Get Profile"
      desc = "Retrieves a user's profile." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the user to retrieve."/>
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "UserProfileDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/user/jonny1/profile",
                       "value":
                         {
                           "id": "jonny1",
                           "firstName": "John",
                           "lastName": "Doe",
                           "email": "anEmailAddress"
                         }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Execution with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>