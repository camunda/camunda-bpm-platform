<#macro dto_macro docsUrl="">
<@lib.dto>

  <@lib.property
      name = "errorCode"
      type = "string"
      desc = "An error code that indicates the predefined error. It is used to identify the BPMN
              error handler." />

  <@lib.property
      name = "errorMessage"
      type = "string"
      desc = "An error message that describes the error." />

  <@lib.property
      name = "variables"
      type = "object"
      dto = "VariableValueDto"
      additionalProperties = true
      last =  true
      desc = "A JSON object containing variable key-value pairs." />

</@lib.dto>

</#macro>