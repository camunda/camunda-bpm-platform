{
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "description": "The id of the decision definition."
    },
    "key": {
      "type": "string",
      "description": "The key of the decision definition, i.e., the id of the DMN 1.0 XML decision definition."
    },
    "category": {
      "type": "string",
      "description": "The category of the decision definition."
    },
    "name": {
      "type": "string",
      "description": "The name of the decision definition."
    },
    "version": {
      "type": "integer",
      "format": "int32",
      "description": "The version of the decision definition that the engine assigned to it."
    },
    "resource": {
      "type": "string",
      "description": "The file name of the decision definition."
    },
    "deploymentId": {
      "type": "string",
      "description": "The deployment id of the decision definition."
    },
    "tenantId": {
      "type": "string",
      "description": "The tenant id of the decision definition."
    },
    "decisionRequirementsDefinitionId": {
      "type": "string",
      "description": "The id of the decision requirements definition this decision definition belongs to."
    },
    "decisionRequirementsDefinitionKey": {
      "type": "string",
      "description": "The key of the decision requirements definition this decision definition belongs to."
    },
    "historyTimeToLive": {
      "type": "integer",
      "description": "History time to live value of the decision definition. Is used within History cleanup.",
      "format": "int32",
      "minimum": 0
    },
    "versionTag": {
      "type": "string",
      "description": "The version tag of the process definition."
    }
  }
}