<#macro dto_macro docsUrl="">
<@lib.dto
    title = "LockedExternalTaskDto" >

  <@lib.property
      name = "activityId"
      type = "string"
      desc = "The id of the activity that this external task belongs to." />

  <@lib.property
      name = "activityInstanceId"
      type = "string"
      desc = "The id of the activity instance that the external task belongs to." />

  <@lib.property
      name = "errorMessage"
      type = "string"
      desc = "The full error message submitted with the latest reported failure executing this task;`null` if no failure
              was reported previously or if no error message was submitted" />

  <@lib.property
      name = "errorDetails"
      type = "string"
      desc = "The error details submitted with the latest reported failure executing this task.`null` if no failure was
              reported previously or if no error details was submitted" />

  <@lib.property
      name = "executionId"
      type = "string"
      desc = "The id of the execution that the external task belongs to." />

  <@lib.property
      name = "id"
      type = "string"
      desc = "The id of the external task." />

  <@lib.property
      name = "lockExpirationTime"
      type = "string"
      format = "date-time"
      desc = "The date that the task's most recent lock expires or has expired." />

  <@lib.property
      name = "processDefinitionId"
      type = "string"
      desc = "The id of the process definition the external task is defined in." />

  <@lib.property
      name = "processDefinitionKey"
      type = "string"
      desc = "The key of the process definition the external task is defined in." />

  <@lib.property
      name = "processDefinitionVersionTag"
      type = "string"
      desc = "The version tag of the process definition the external task is defined in." />

  <@lib.property
      name = "processInstanceId"
      type = "string"
      desc = "The id of the process instance the external task belongs to." />

  <@lib.property
      name = "tenantId"
      type = "string"
      desc = "The id of the tenant the external task belongs to." />

  <@lib.property
      name = "retries"
      type = "integer"
      format = "int32"
      desc = "The number of retries the task currently has left." />

  <@lib.property
      name = "suspended"
      type = "boolean"
      desc = "Whether the process instance the external task belongs to is suspended." />

  <@lib.property
      name = "workerId"
      type = "string"
      desc = "The id of the worker that posesses or posessed the most recent lock." />

  <@lib.property
      name = "priority"
      type = "integer"
      format = "int64"
      desc = "The priority of the external task." />

  <@lib.property
      name = "topicName"
      type = "string"
      desc = "The topic name of the external task." />

  <@lib.property
      name = "businessKey"
      type = "string"
      desc = "The business key of the process instance the external task belongs to." />

  <@lib.property
      name = "variables"
      type = "object"
      dto = "VariableValueDto"
      additionalProperties = true
      last = true
      desc = "A JSON object containing a property for each of the requested variables. The key is the variable name,
              the value is a JSON object of serialized variable values with the following properties:" />

</@lib.dto>

</#macro>