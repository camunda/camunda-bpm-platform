<#macro dto_macro docsUrl="">
<@lib.dto>

  <@lib.property
      name = "priority"
      type = "integer"
      format = "int64"
      last = true
      desc = "The priority of the resource." />

</@lib.dto>

</#macro>