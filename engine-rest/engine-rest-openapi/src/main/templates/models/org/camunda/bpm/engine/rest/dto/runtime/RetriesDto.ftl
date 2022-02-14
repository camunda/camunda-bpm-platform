<#macro dto_macro docsUrl="">
<@lib.dto>

  <@lib.property
      name = "retries"
      type = "integer"
      format = "int32"
      last = true
      desc = "The number of retries to set for the resource.  Must be >= 0. If this is 0, an incident is created
              and the task, or job, cannot be fetched, or acquired anymore unless the retries are increased again.
              Can not be null." />


</@lib.dto>

</#macro>