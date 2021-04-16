<#macro dto_macro docsUrl="">
<@lib.dto extends = "HandleExternalTaskDto" >

  <@lib.property
      name = "newDuration"
      type = "integer"
      format = "int64"
      last = true
      desc = "An amount of time (in milliseconds). This is the new lock duration starting from the
              current moment." />

</@lib.dto>

</#macro>