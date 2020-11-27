<@lib.dto>

    <@lib.property
        name = "valid"
        type = "boolean"
        desc = "`true` if the password is compliant with the policy, otherwise `false`"
    />

    <@lib.property
        name = "rules"
        type = "array"
        dto = "CheckPasswordPolicyRuleDto"
        last = true
        desc = "An array of password policy rules. Each element of the array is representing one rule of the policy."
    />

</@lib.dto>
