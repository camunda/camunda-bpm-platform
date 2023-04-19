<#macro dto_macro docsUrl="">
<@lib.dto >
  <#assign noteCountReport = "**Note:** This property is only set for a historic task report object.
                              In these cases, the value of the `reportType` query parameter is `count`." />
  <#assign noteDurationReport = "**Note:** This property is only set for a duration report object.
                                 In these cases, the value of the `reportType` query parameter is `duration`." />

  <@lib.property
      name = "taskName"
      type = "string"
      desc = "The name of the task. It is only available when the `groupBy` parameter is set to `taskName`.
              Else the value is `null`.

              ${noteCountReport}"
  />

  <@lib.property
      name = "count"
      type = "integer"
      format = "int64"
      desc = "The number of tasks which have the given definition.

              ${noteCountReport}"
  />

  <@lib.property
      name = "processDefinitionKey"
      type = "string"
      desc = "The key of the process definition.

              ${noteCountReport}"
  />

  <@lib.property
      name = "processDefinitionId"
      type = "string"
      desc = "The id of the process definition.

              ${noteCountReport}"
  />

  <@lib.property
      name = "processDefinitionName"
      type = "string"
      desc = "The name of the process definition.

              ${noteCountReport}"
  />

  <@lib.property
      name = "period"
      type = "integer"
      format = "int32"
      desc = "Specifies a span of time within a year.
              **Note:** The period must be interpreted in conjunction with the returned `periodUnit`.

              ${noteDurationReport}"
  />

  <@lib.property
      name = "periodUnit"
      type = "string"
      enumValues = ["MONTH", "QUARTER"]
      desc = "The unit of the given period. Possible values are `MONTH` and `QUARTER`.

              ${noteDurationReport}"
  />

  <@lib.property
      name = "minimum"
      type = "integer"
      format = "int64"
      desc = "The smallest duration in milliseconds of all completed process instances which
              were started in the given period.

              ${noteDurationReport}"
  />

  <@lib.property
      name = "maximum"
      type = "integer"
      format = "int64"
      desc = "The greatest duration in milliseconds of all completed process instances which were
              started in the given period.

              ${noteDurationReport}"
  />

  <@lib.property
      name = "average"
      type = "integer"
      format = "int64"
      desc = "The average duration in milliseconds of all completed process instances which were
              started in the given period.

              ${noteDurationReport}"
  />

  <@lib.property
      name = "tenantId"
      type = "string"
      last = true
      desc = "The id of the tenant." />

</@lib.dto>
</#macro>
