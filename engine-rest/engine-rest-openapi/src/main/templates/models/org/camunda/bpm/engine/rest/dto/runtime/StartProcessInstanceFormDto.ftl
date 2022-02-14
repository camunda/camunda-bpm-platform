<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto" />

    <@lib.property
        name = "businessKey"
        type = "string"
        last = true
        desc = "The business key the process instance is to be initialized with.
                The business key uniquely identifies the process instance in the context of the given process definition." />

</@lib.dto>
</#macro>