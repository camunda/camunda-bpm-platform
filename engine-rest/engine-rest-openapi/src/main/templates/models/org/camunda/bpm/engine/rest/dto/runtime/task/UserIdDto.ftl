<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "userId"
        type = "string"
        last =  true
        desc = "The id of the user that the current action refers to." />

</@lib.dto>
</#macro>