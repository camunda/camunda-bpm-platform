<#macro dto_macro docsUrl="">
<@lib.dto>

  <#-- NOTE: Please consider adjusting the `HistoricTaskInstanceReportResultDto.ftl` file,
       if the properties are valid there as well. The DTO was created separately as it is
       included in a `oneOf` relation for the Historic Task Instance endpoints -->

  <@lib.property
      name = "period"
      type = "integer"
      format = "int32"
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
      desc = "The smallest duration in milliseconds of all completed process instances which were started in the given period."/>

  <@lib.property
      name = "maximum"
      type = "integer"
      format = "int64"
      desc = "The greatest duration in milliseconds of all completed process instances which were started in the given period."/>

  <@lib.property
      name = "average"
      type = "integer"
      format = "int64"
      last = true
      desc = "The average duration in milliseconds of all completed process instances which were started in the given period."/>

</@lib.dto>
</#macro>