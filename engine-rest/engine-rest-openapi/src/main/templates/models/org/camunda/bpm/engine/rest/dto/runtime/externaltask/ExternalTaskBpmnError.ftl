<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "TaskBpmnErrorDto" >

  <@lib.property
      name = "workerId"
      type = "string"
      last = true
      desc = "The id of the worker that reports the failure. Must match the id of the worker who has most recently
              locked the task." />

</@lib.dto>

</#macro>