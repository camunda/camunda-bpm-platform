<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/identity/verify-user/index.html -->
{
  <@lib.endpointInfo
      id = "verifyUser"
      tag = "Identity"
      summary = "Verify User"
      desc = "Verifies that user credentials are valid."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "BasicUserCredentialsDto"
      examples = ['"example-1": {
                     "summary": "POST `/identity/verify`",
                     "value": {
                       "username": "testUser",
                       "password": "testPassword"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "AuthenticationResult"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/identity/verify`",
                       "value": {
                         "authenticatedUser": "testUser",
                         "authenticated": true
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "If body does not contain username or password."
        last = true
    />

  }

}
</#macro>