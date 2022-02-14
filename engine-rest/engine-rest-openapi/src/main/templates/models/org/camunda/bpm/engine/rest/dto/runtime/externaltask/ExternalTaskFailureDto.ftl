<#macro dto_macro docsUrl="">
<@lib.dto extends = "HandleExternalTaskDto" >

  <@lib.property
      name = "errorMessage"
      type = "string"
      desc = "An message indicating the reason of the failure." />

  <@lib.property
      name = "errorDetails"
      type = "string"
      desc = "A detailed error description." />

  <@lib.property
      name = "retries"
      type = "integer"
      format = "int32"
      desc = "A number of how often the task should be retried. Must be >= 0. If this is 0, an incident is created and
              the task cannot be fetched anymore unless the retries are increased again. The incident's message is set
              to the `errorMessage` parameter." />

  <@lib.property
      name = "retryTimeout"
      type = "integer"
      format = "int64"
      desc = "A timeout in milliseconds before the external task becomes available again for fetching. Must be >= 0." />

<@lib.property
      name = "variables"
      type = "object"
      additionalProperties = true
      dto = "VariableValueDto"
      desc = "A JSON object containing variable key-value pairs. Each key is a variable name and each value a JSON variable value object with the following properties:" />

  <@lib.property
      name = "localVariables"
      type = "object"
      additionalProperties = true
      last = true
      dto = "VariableValueDto"
      desc = "A JSON object containing local variable key-value pairs. Local variables are set only in the scope of external task. Each key is a variable name and each value a JSON variable value object with the following properties:" />

</@lib.dto>

</#macro>