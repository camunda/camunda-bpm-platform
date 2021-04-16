<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids to restart." />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        dto = "HistoricProcessInstanceQueryDto"
        desc = "A historic process instance query." />

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        desc = "Skip execution listener invocation for activities that are started as part of this request." />

    <@lib.property
        name = "skipIoMappings"
        type = "boolean"
        desc = "Skip execution of
                [input/output variable mappings](${docsUrl}/user-guide/process-engine/variables/#input-output-variable-mapping)
                for activities that are started as part of this request." />
                

    <@lib.property
        name = "initialVariables"
        type = "boolean"
        desc = "Set the initial set of variables during restart. By default, the last set of variables is used." />

    <@lib.property
        name = "withoutBusinessKey"
        type = "boolean"
        desc = "Do not take over the business key of the historic process instance." />

    <@lib.property
        name = "instructions"
        type = "array"
        dto = "RestartProcessInstanceModificationInstructionDto"
        last = true
        desc = "**Optional**. A JSON array of instructions that specify which activities to start the process instance at.
                If this property is omitted, the process instance starts at its default blank start event." />

</@lib.dto>
</#macro>