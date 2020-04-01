<@lib.dto>

  <@lib.property
      name = "workerId"
      type = "string"
      desc = "The id of the worker that completes the task. Must match the id of the worker who has most recently locked the task." />

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
