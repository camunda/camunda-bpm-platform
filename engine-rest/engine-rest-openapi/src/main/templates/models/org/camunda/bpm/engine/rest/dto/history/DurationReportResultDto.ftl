<@lib.dto>

  <@lib.property
      name = "period"
      type = "integer"
      format = "int32"
      nullable = false
      desc = "Specifies a timespan within a year.
              **Note:** The period must be interpreted in conjunction with the returned `periodUnit`."/>

  <@lib.property
      name = "periodUnit"
      type = "string"
      enumValues = ["MONTH", "QUARTER"]
      desc = "The unit of the given period. Possible values are `MONTH` and `QUARTER`."/>

  <@lib.property
      name = "minimum"
      type = "integer"
      format = "int64"
      nullable = false
      desc = "The smallest duration in milliseconds of all completed process instances which were started in the given period."/>

  <@lib.property
      name = "maximum"
      type = "integer"
      format = "int64"
      nullable = false
      desc = "The greatest duration in milliseconds of all completed process instances which were started in the given period."/>

  <@lib.property
      name = "average"
      type = "integer"
      format = "int64"
      nullable = false
      last = true
      desc = "The average duration in milliseconds of all completed process instances which were started in the given period."/>

</@lib.dto>