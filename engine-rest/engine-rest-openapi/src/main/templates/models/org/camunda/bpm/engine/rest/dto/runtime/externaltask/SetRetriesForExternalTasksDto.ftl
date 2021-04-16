<#macro dto_macro docsUrl="">
<@lib.dto>

  <@lib.property
      name = "retries"
      type = "integer"
      format = "int32"
      desc = "The number of retries to set for the external task.  Must be >= 0. If this is 0, an incident is created
              and the task cannot be fetched anymore unless the retries are increased again. Can not be null." />

  <@lib.property
      name = "externalTaskIds"
      type = "array"
      itemType = "string"
      desc = "The ids of the external tasks to set the number of retries for." />

  <@lib.property
      name = "processInstanceIds"
      type = "array"
      itemType = "string"
      desc = "The ids of process instances containing the tasks to set the number of retries for." />

  <@lib.property
      name = "externalTaskQuery"
      type = "ref"
      dto = "ExternalTaskQueryDto"
      desc = "Query for the external tasks to set the number of retries for." />

  <@lib.property
      name = "processInstanceQuery"
      type = "ref"
      dto = "ProcessInstanceQueryDto"
      desc = "Query for the process instances containing the tasks to set the number of retries for." />

  <@lib.property
      name = "historicProcessInstanceQuery"
      type = "ref"
      dto = "HistoricProcessInstanceQueryDto"
      last = true
      desc = "Query for the historic process instances containing the tasks to set the number of retries for." />


</@lib.dto>

</#macro>