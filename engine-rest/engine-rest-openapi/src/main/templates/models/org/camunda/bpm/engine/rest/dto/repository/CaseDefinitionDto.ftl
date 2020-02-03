{
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "description": "The id of the case definition"
    },
    "key": {
      "type": "string",
      "description": "The key of the case definition, i.e., the id of the CMMN 2.0 XML case definition."
    },
    "category": {
      "type": "string",
      "description": "The category of the case definition."
    },
    "name": {
      "type": "string",
      "description": "The name of the case definition."
    },
    "version": {
      "type": "integer",
      "format": "int32",
      "description": "The version of the case definition that the engine assigned to it."
    },
    "resource": {
      "type": "string",
      "description": "The file name of the case definition."
    },
    "deploymentId": {
      "type": "string",
      "description": "The deployment id of the case definition."
    },
    "tenantId": {
      "type": "string",
      "description": "The tenant id of the case definition."
    },
    "historyTimeToLive": {
      "type": "integer",
      "description": "History time to live value of the case definition. Is used within History cleanup.",
      "format": "int32",
      "minimum": 0
    }
  }
 
}