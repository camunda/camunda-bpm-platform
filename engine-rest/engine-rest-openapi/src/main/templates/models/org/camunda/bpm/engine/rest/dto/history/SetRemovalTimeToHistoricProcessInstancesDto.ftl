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
      desc = "Sets the removal time to all historic process instances in the hierarchy.
              Value may only be `true`, as `false` is the default behavior."/>

  <@lib.property
      name = "updateInChunks"
      type = "boolean"
      desc = "Processes removal time updates in chunks, taking into account defined limits.
              This can lead to multiple executions of the resulting jobs and prevents the database
              transaction from timing out by limiting the number of rows to update.
              Value may only be `true`, as `false` is the default behavior."/>

  <@lib.property
      name = "updateChunkSize"
      type = "integer"
      format = "int32"
      last = true
      desc = "Defines the size of the chunks in which removal time updates are processed.
              The value must be a positive integer between `1` and `500`.
              This only has an effect if `updateInChunks` is set to `true`."/>

</@lib.dto>
</#macro>