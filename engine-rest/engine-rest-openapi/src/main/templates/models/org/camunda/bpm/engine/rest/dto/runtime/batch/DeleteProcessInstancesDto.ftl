<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list process instance ids to delete." />

    <@lib.property
        name = "deleteReason"
        type = "string"
        desc = "A string with delete reason." />

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        desc = "Skip execution listener invocation for activities that are started or ended as part of this request." />

    <@lib.property
        name = "skipSubprocesses"
        type = "boolean"
        desc = "Skip deletion of the subprocesses related to deleted processes as part of this request." />

    <@lib.property
        name = "skipIoMappings"
        type = "boolean"
        desc = "Skip execution of [input/output variable mappings](${docsUrl}/user-guide/process-engine/variables/#input-output-variable-mapping)
                for activities that are started or ended as part of this request." />

    <@lib.property
        name = "processInstanceQuery"
        type = "ref"
        dto = "ProcessInstanceQueryDto" />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        last = true
        dto = "HistoricProcessInstanceQueryDto" />

</@lib.dto>
</#macro>