<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "TaskDto" >

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        last = true
        desc = "Holds the appropriate variables for the task." />

</@lib.dto>
</#macro>