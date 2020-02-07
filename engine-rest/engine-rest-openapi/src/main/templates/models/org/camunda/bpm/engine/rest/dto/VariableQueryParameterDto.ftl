{
  "title": "VariableQueryParameterDto",
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "description": "Variable name"
    },
    "opearator": {
      "type": "string",
      "description": "comparison operator to be used",
      "enum": [
        "eq",
        "neq",
        "gt",
        "gteq",
        "lt",
        "lteq",
        "like"
      ]
    },
    "value": {
      "oneOf": [
        {"type": "boolean"},
        {"type": "string"},
        {"type": "number"}
      ],
      "description": "the variable value"
    }
  }
}