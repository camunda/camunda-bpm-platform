<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "createUser"
      tag = "User"
      summary = "Create"
      desc = "Create a new user." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "UserDto"
      examples = ['"example-1": {
                     "summary": "POST /user/create",
                     "value": {
                         "profile": {
                             "id": "jonny1",
                             "firstName": "John",
                             "lastName": "Doe",
                             "email": "anEmailAddress"
                             },
                         "credentials": {
                            "password": "s3cret"
                        }
                   }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        last = true
        desc = "Request successful." />
  }
}
</#macro>