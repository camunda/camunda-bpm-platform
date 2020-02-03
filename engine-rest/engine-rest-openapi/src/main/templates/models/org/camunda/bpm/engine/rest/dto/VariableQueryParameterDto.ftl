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
      "type": "string",
      <#-- TODO
      [
        "string",
        "number",
        "boolean"
      ],
       -->
      "description": "the variable value"
    }
  }
}