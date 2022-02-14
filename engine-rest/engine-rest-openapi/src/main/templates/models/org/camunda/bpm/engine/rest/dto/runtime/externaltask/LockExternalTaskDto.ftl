<#macro dto_macro docsUrl="">
<@lib.dto extends = "HandleExternalTaskDto" >

  <@lib.property
      name = "lockDuration"
      type = "integer"
      format = "int64"
      nullable = false
      last = true
      desc = "The duration to lock the external task for in milliseconds.
              **Note:** Attempting to lock an already locked external task with the same `workerId`
              will succeed and a new lock duration will be set, starting from the current moment." />

</@lib.dto>

</#macro>