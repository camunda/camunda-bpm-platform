{
  "type" : "object",
  "properties" : {
    "modifications" : {
      "type" : "object",
      "additionalProperties" : {
        "$ref" : "#/components/schemas/VariableValueDto"
      },
      "description": "A JSON object containing variable key-value pairs."
    },
    "deletions" : {
      "type" : "array",
      "description": "An array of String keys of variables to be deleted.",
      "items" : {
        "type" : "string"
      }
    }
  }
}