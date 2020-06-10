<@lib.dto>

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
      name = "absoluteRemovalTime"
      type = "string"
      format = "date-time"
      nullable = false
      desc = "The date for which the historic process instances shall be removed. Value may not be `null`.

              **Note:** Cannot be set in conjunction with `clearedRemovalTime` or `calculatedRemovalTime`."/>

  <@lib.property
      name = "clearedRemovalTime"
      type = "boolean"
      desc = "Sets the removal time to `null`. Value may only be `true`, as `false` is the default behavior.

              **Note:** Cannot be set in conjunction with `absoluteRemovalTime` or `calculatedRemovalTime`."/>

  <@lib.property
      name = "calculatedRemovalTime"
      type = "boolean"
      last = true
      desc = "The removal time is calculated based on the engine's configuration settings. Value may only be `true`, as `false` is the default behavior.

              **Note:** Cannot be set in conjunction with `absoluteRemovalTime` or `clearedRemovalTime`."/>

</@lib.dto>