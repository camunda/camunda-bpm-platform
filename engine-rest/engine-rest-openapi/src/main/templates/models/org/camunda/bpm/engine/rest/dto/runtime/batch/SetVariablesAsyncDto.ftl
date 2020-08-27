<@lib.dto>

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids that define a group of process instances
                to which the operation will set variables." />

    <@lib.property
        name = "processInstanceQuery"
        type = "ref"
        dto = "ProcessInstanceQueryDto" />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        dto = "HistoricProcessInstanceQueryDto" />

      <@lib.property
          name = "variables"
          type = "object"
          additionalProperties = true
          last = true
          dto = "VariableValueDto" />

</@lib.dto>