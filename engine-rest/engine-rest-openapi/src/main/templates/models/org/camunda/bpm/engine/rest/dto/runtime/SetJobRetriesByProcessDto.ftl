<#macro dto_macro docsUrl="">
<@lib.dto 
    extends = "SetJobRetriesDto">

    <@lib.property
        name = "processInstances"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids to fetch jobs, for which retries will be set." />

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