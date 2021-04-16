<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "processInstances"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids to fetch jobs, for which retries will be set." />

    <@lib.property
        name = "retries"
        type = "integer"
        format = "int32"
        minimum = 0
        desc = "An integer representing the number of retries. Please note that the value cannot be negative or null." />

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