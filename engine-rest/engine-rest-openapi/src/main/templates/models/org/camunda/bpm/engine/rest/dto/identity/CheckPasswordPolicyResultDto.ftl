<#macro dto_macro docsUrl="">
<@lib.dto extends="PasswordPolicyDto">

    <@lib.property
        name = "valid"
        type = "boolean"
        desc = "`true` if the password is compliant with the policy, otherwise `false`."
        last = true
    />

</@lib.dto>

</#macro>