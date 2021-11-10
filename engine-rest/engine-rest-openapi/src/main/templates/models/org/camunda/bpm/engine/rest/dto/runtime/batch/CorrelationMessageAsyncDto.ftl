<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "messageName"
        type = "string"
        desc = "The name of the message to correlate. Corresponds to the 'name' element of the message defined in BPMN 2.0 XML. Can be null to correlate by other criteria only." />

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids that define a group of process instances
                to which the operation will correlate a message.

                Please note that if `processInstanceIds`, `processInstanceQuery` and `historicProcessInstanceQuery`
                are defined, the resulting operation will be performed on the union of these sets." />

    <@lib.property
        name = "processInstanceQuery"
        type = "ref"
        dto = "ProcessInstanceQueryDto"
        desc = "A process instance query to select process instances the operation will correlate a message to." />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        dto = "HistoricProcessInstanceQueryDto"
        desc= "A historic process instance query to select process instances the operation will correlate a message to." />

      <@lib.property
          name = "variables"
          type = "object"
          additionalProperties = true
          last = true
          dto = "VariableValueDto"
          desc= "All variables the operation will set in the root scope of the process instances the message is correlated to." />

</@lib.dto>
</#macro>