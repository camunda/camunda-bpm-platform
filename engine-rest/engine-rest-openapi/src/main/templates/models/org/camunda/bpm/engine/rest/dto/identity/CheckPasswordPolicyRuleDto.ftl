<#macro dto_macro docsUrl="">
<@lib.dto extends =  "PasswordPolicyRuleDto">

    <@lib.property
        name = "valid"
        type = "boolean"
        desc = "`true` if the password is compliant with this rule, otherwise `false`."
        last = true
    />

</@lib.dto>

</#macro>