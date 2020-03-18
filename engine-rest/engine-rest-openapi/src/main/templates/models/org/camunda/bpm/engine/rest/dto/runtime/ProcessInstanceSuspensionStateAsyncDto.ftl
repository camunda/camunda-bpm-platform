<@lib.dto>

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A Boolean value which indicates whether to activate or suspend a given process instance.
                When the value is set to true, the given process instance will be suspended and when the value is set to false,
                the given process instance will be activated." />

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