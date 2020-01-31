{
  "type": "object",
  "properties": {
    "sortBy": {
      "type": "string",
      "description": "Sort the results lexicographically by a given criterion.",
      "enum": [
        "instanceId",
        "definitionKey",
        "definitionId",
        "tenantId",
        "businessKey"
      ]
    },
    "sortOrder": {
      "type": "string",
      "description": "Sort the results in a given order. Values might be asc for ascending order or desc for descending order.",
      "enum": [
        "asc",
        "desc"
      ]
    }
  }
}