<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids that define a group of process instances
                to which the operation will set variables.

                Please note that if `processInstanceIds`, `processInstanceQuery` and `historicProcessInstanceQuery`
                are defined, the resulting operation will be performed on the union of these sets." />

    <@lib.property
        name = "processInstanceQuery"
        type = "ref"
        dto = "ProcessInstanceQueryDto"
        desc = "A process instance query to select process instances the operation will set variables to." />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        dto = "HistoricProcessInstanceQueryDto"
        desc= "A historic process instance query to select process instances the operation will set variables to." />

      <@lib.property
          name = "variables"
          type = "object"
          additionalProperties = true
          last = true
          dto = "VariableValueDto"
          desc= "A variables the operation will set in the root scope of the process instances." />

</@lib.dto>
</#macro>