<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "profile"
        type = "ref"
        dto = "UserProfileDto"
        desc = "A JSON object containing variable key-value pairs.
        The object contains the following properties:
        id (String), firstName (String), lastName (String) and email (String). "/>

    <@lib.property
        name = "credentials"
        type = "ref"
        dto = "UserCredentialsDto"
        last = true
        desc = "A JSON object containing variable key-value pairs.
        The object contains the following property: password (String).  "/>

</@lib.dto>

</#macro>