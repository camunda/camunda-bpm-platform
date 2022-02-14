<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "password"
        type = "string"
        desc = "The candidate password to be check against the password policy."
    />

    <@lib.property
        name = "profile"
        type = "ref"
        dto = "UserProfileDto"
        last = true
        desc = "A JSON object containing variable key-value pairs. The object can contain the following properties:
                id (String), firstName (String), lastName (String) and email (String)."
    />

</@lib.dto>

</#macro>