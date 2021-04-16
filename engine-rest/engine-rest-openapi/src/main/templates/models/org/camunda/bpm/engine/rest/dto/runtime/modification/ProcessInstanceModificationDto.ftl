<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        desc = "Skip execution listener invocation for activities that are started or ended as part of this request." />

    <@lib.property
        name = "skipIoMappings"
        type = "boolean"
        desc = "Skip execution of [input/output variable mappings](${docsUrl}/user-guide/process-engine/variables/#input-output-variable-mapping)
                for activities that are started or ended as part of this request." />

    <@lib.property
        name = "instructions"
        type = "array"
        dto = "ProcessInstanceModificationInstructionDto"
        desc = "JSON array of modification instructions. The instructions are executed in the order they are in." />

    <@lib.property
        name = "annotation"
        type = "string"
        last = true
        desc = "An arbitrary text annotation set by a user for auditing reasons." />

</@lib.dto>
</#macro>