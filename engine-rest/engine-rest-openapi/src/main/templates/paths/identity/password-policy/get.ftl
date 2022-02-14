<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/identity/get-password-policy/index.html -->
{
  <@lib.endpointInfo
      id = "getPasswordPolicy"
      tag = "Identity"
      summary = "Get Password Policy"
      desc = "A password policy consists of a list of rules that new passwords must follow to be
              policy compliant. This end point returns a JSON representation of the
              list of policy rules. More information on password policies in Camunda can be found in the password policy
              [user guide](${docsUrl}/user-guide/process-engine/password-policy/) and in
              the [security instructions](${docsUrl}/user-guide/security/)."
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "PasswordPolicyDto"
        desc = "Request successful. This example uses the built-in password policy that enforces a minimum password length,
                and some complexity rules."
        examples = ['"example-1": {
                       "description": "GET `/identity/password-policy`",
                       "value": {
                         "rules": [
                           {
                             "placeholder": "PASSWORD_POLICY_USER_DATA",
                             "parameter": null
                           },
                           {
                             "placeholder": "PASSWORD_POLICY_LENGTH",
                             "parameter": {
                               "minLength": "10"
                             }
                           },
                           {
                             "placeholder": "PASSWORD_POLICY_LOWERCASE",
                             "parameter": {
                               "minLowerCase": "1"
                             }
                           },
                           {
                             "placeholder": "PASSWORD_POLICY_UPPERCASE",
                             "parameter": {
                               "minUpperCase": "1"
                             }
                           },
                           {
                             "placeholder": "PASSWORD_POLICY_DIGIT",
                             "parameter": {
                               "minDigit": "1"
                             }
                           },
                           {
                             "placeholder": "PASSWORD_POLICY_SPECIAL",
                             "parameter": {
                               "minSpecial": "1"
                             }
                           }
                         ]
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "No password policy was found."
        last = true
    />

  }

}
</#macro>