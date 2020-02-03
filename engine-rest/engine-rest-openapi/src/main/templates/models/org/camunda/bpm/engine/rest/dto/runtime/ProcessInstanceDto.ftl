{
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "description": "The id of the process instance."
    },
    "definitionId": {
      "type": "string",
      "description": "The id of the process definition that this process instance belongs to."
    },
    "businessKey": {
      "type": "string",
      "description": "The business key of the process instance."
    },
    "caseInstanceId": {
      "type": "string",
      "description": "The id of the case instance associated with the process instance."
    },
    "ended": {
      "type": "boolean",
      "description": "A flag indicating whether the process instance has ended or not. Deprecated: will always be false!",
      "deprecated": true
    },
    "suspended": {
      "type": "boolean",
      "description": "A flag indicating whether the process instance is suspended or not."
    },
    "tenantId": {
      "type": "string",
      "description": "The tenant id of the process instance."
    },
    "links": {
      "type": "array",
      "items": {
        "$ref": "#/components/schemas/AtomLink"
      }
    }
  }
}