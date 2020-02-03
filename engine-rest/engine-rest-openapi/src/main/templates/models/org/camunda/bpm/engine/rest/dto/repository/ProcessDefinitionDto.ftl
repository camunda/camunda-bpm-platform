{
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "description": "The id of the process definition."
    },
    "key": {
      "type": "string",
      "description": "The key of the process definition, i.e., the id of the BPMN 2.0 XML process definition."
    },
    "category": {
      "type": "string",
      "description": "The category of the process definition."
    },
    "description": {
      "type": "string",
      "description": "The description of the process definition."
    },
    "name": {
      "type": "string",
      "description": "The name of the process definition."
    },
    "version": {
      "type": "integer",
      "format": "int32",
      "description": "The version of the process definition that the engine assigned to it."
    },
    "resource": {
      "type": "string",
      "description": "The file name of the process definition."
    },
    "deploymentId": {
      "type": "string",
      "description": "The deployment id of the process definition."
    },
    "diagram": {
      "type": "string",
      "description": "The file name of the process definition diagram, if it exists."
    },
    "suspended": {
      "type": "boolean",
      "description": "A flag indicating whether the definition is suspended or not."
    },
    "tenantId": {
      "type": "string",
      "description": "The tenant id of the process definition."
    },
    "versionTag": {
      "type": "string",
      "description": "The version tag of the process definition."
    },
    "historyTimeToLive": {
      "type": "integer",
      "description": "History time to live value of the process definition. Is used within History cleanup.",
      "format": "int32",
      "minimum": 0
    },
    "startableInTasklist": {
      "type": "boolean",
      "description": "A flag indicating whether the process definition is startable in Tasklist or not."
    }
  }
}