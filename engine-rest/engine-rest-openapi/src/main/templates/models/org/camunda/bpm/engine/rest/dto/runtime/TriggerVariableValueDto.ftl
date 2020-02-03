{
  "allOf": [
    {
      "$ref": "#/components/schemas/VariableValueDto"
    },
    {
      "type": "object",
      "properties": {
        "local": {
          "type": "boolean",
          "description": "Indicates whether the variable should be a local variable or not. If set to true, the variable becomes a local variable of the execution entering the target activity."
        }
      }
    }
  ]
}