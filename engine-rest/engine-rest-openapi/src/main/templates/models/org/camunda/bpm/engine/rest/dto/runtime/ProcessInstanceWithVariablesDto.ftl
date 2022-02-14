<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "ProcessInstanceDto" >

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        last = true
        desc = "The id of the process instance." />

</@lib.dto>
</#macro>