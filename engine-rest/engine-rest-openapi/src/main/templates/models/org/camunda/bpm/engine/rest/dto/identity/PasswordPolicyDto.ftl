<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "rules"
        type = "array"
        dto = "PasswordPolicyRuleDto"
        desc = "An array of password policy rules. Each element of the array is representing one rule of the policy."
        last = true
    />

</@lib.dto>
</#macro>