<#-- Generated From File: camunda-docs-manual/public/reference/rest/modification/post-modification-sync/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto desc = "">
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition for the modification"
    />

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        desc = "Skip execution listener invocation for activities that are started or ended as part
                of this request."
    />

    <@lib.property
        name = "skipIoMappings"
        type = "boolean"
        desc = "Skip execution of [input/output variable mappings](${docsUrl}/user-guide/process-engine/variables/#input-output-variable-mapping) for
                activities that are started or ended as part of this request."
    />

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids to modify."
    />

    
    <@lib.property
        name = "processInstanceQuery"
        type = "ref"
        dto = "ProcessInstanceQueryDto"
        desc = "A process instance query."
    />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        dto = "HistoricProcessInstanceQueryDto"
        desc = "A historic process instance query. It is advised to include the `unfinished` filter in the
                historic process instance query as finished instances cause failures for the modification."
    />

    <@lib.property
        name = "instructions"
        type = "array"
        dto = "MultipleProcessInstanceModificationInstructionDto"
        desc = "An array of modification instructions. The instructions are executed in the order they are in. "
    />

    <@lib.property
        name = "annotation"
        type = "string"
        last = true
        desc = "An arbitrary text annotation set by a user for auditing reasons."
    />

</@lib.dto>
</#macro>
