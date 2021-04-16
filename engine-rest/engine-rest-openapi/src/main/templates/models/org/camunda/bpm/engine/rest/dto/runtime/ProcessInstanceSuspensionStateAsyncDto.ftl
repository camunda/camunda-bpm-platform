<#macro dto_macro docsUrl="">
<@lib.dto extends = "SuspensionStateDto">

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids which defines a group of process instances
                which will be activated or suspended by the operation." />

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