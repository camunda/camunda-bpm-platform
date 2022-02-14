<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "modifications"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A JSON object containing variable key-value pairs." />

    <@lib.property
        name = "deletions"
        type = "array"
        itemType = "string"
        last = true
        desc = "An array of String keys of variables to be deleted."/>

</@lib.dto>
</#macro>