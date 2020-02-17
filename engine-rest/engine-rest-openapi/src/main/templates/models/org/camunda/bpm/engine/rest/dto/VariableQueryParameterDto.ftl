{
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
      "type": "object",
      "description": "the variable value, could be of type boolean, string or number"
    }
  }
}