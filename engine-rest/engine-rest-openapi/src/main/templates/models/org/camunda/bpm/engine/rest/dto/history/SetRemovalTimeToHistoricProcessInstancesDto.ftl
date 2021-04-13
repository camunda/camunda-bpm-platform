<#macro dto_macro docsUrl="">
<@lib.dto extends = "AbstractSetRemovalTimeDto">

  <@lib.property
      name = "historicProcessInstanceIds"
      type = "array"
      itemType = "string"
      desc = "The id of the process instance."/>

  <@lib.property
      name = "historicProcessInstanceQuery"
      type = "ref"
      dto = "HistoricProcessInstanceQueryDto"
      desc = "Query for the historic process instances to set the removal time for."/>

  <@lib.property
      name = "hierarchical"
      type = "boolean"
      last = true
      desc = "Sets the removal time to all historic process instances in the hierarchy.
              Value may only be `true`, as `false` is the default behavior."/>

</@lib.dto>
</#macro>