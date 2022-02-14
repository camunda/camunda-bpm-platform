<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-signal/index.html -->
<@lib.dto desc = "">
    
    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A JSON object containing variable key-value pairs. Each key is a variable name and
                each value a JSON variable value object."
        last = true
    />


</@lib.dto>
</#macro>