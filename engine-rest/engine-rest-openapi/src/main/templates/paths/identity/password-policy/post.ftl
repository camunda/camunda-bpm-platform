<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/identity/validate-password/index.html -->
{
  <@lib.endpointInfo
      id = "checkPassword"
      tag = "Identity"
      summary = "Validate Password"
      desc = "A password policy consists of a list of rules that new passwords must follow to be
              policy compliant. A password can be checked for compliancy via this
              end point. More information on password policies in Camunda can be found in the password policy
              [user guide](${docsUrl}/user-guide/process-engine/password-policy/) and in
              the [security instructions](${docsUrl}/user-guide/security/)."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "PasswordPolicyRequestDto"
      examples = ['"example-1": {
                     "summary": "POST `/identity/password-policy`",
                     "value": {
                       "password": "myPassword",
                       "profile": {
                         "id": "jonny1",
                         "firstName": "John",
                         "lastName": "Doe",
                         "email": "jonny@camunda.org"
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CheckPasswordPolicyResultDto"
        desc = "Request successful. This example uses the built-in password policy that enforces a minimum password length, and
                some complexity rules. The checked password is myPassword which is not complex enough to match all of
                the policy rules."
        examples = ['"example-1": {
                       "description": "POST `/identity/password-policy`",
                       "value": {
                            "rules": [
                                {
                                   "placeholder": "PASSWORD_POLICY_USER_DATA",
                                   "parameter": null,
                                   "valid": true
                                },
                                {
                                    "placeholder": "PASSWORD_POLICY_LOWERCASE",
                                    "parameter": {"minLowerCase": "1"},
                                    "valid": true
                                },
                                {
                                    "placeholder": "PASSWORD_POLICY_LENGTH",
                                    "parameter": {"minLength": "10"},
                                    "valid": false
                                },
                                {
                                    "placeholder": PASSWORD_POLICY_UPPERCASE",
                                    "parameter": {"minUpperCase": "1"},
                                    "valid": false
                                },
                                {
                                    "placeholder": "PASSWORD_POLICY_DIGIT",
                                    "parameter": {"minDigit": "1"},
                                    "valid": false
                                },
                                {
                                    "placeholder": "PASSWORD_POLICY_SPECIAL",
                                    "parameter": {"minSpecial": "1"},
                                    "valid": false
                                }
                            ],
                            "valid": false
                        }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "No password policy was found to check the password against."
        last = true
    />

  }

}
</#macro>