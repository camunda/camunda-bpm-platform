{
  "allOf": [
    {
      "$ref": "#/components/schemas/MessageCorrelationResultDto"
    },
    {
      "type": "object",
      "properties": {

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "This property is returned if the `variablesInResultEnabled` is set to `true`.
                Contains a list of the process variables. "/>
      }
    }
  ]
}