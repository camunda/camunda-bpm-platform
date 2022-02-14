<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "message"
        type = "string"
        desc = "The message of the problem."/>

    <@lib.property
        name = "line"
        type = "integer"
        format = "int32"
        desc = "The line where the problem occurred."/>


    <@lib.property
        name = "column"
        type = "integer"
        format = "int32"
        desc = "The column where the problem occurred."/>

    <@lib.property
        name = "mainElementId"
        type = "string"
        desc = "The main element id where the problem occurred."/>


    <@lib.property
        name = "elementIds"
        type = "array"
        itemType = "string"
        last = true
        desc = "A list of element id affected by the problem."/>

</@lib.dto>
</#macro>