{
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "description": "The id  of the deployment."
    },
    "tenantId": {
      "type": "string",
      "description": "The tenant id of the deployment."
    },
    "deploymentTime": {
      "type": "string",
      "format": "date-time",
      "description": "The time when the deployment was created."
    },
    "source": {
      "type": "string",
      "description": "The source of the deployment."
    },
    "name": {
      "type": "string",
      "description": "The name of the deployment."
    },
    "links": {
      "type": "array",
      "description": "Link to the newly created deployment with method, href and rel.",
      "items": {
        "$ref": "#/components/schemas/AtomLink"
      }
    },
    "deployedProcessDefinitions": {
      "type": "object",
      "description": "A JSON Object containing a property for each of the process definitions, which are successfully deployed with that deployment. The key is the process definition id, the value is a JSON Object corresponding to the process definition.",
      "additionalProperties": {
        "$ref": "#/components/schemas/ProcessDefinitionDto"
      }
    },
    "deployedDecisionDefinitions": {
      "type": "object",
      "description": "A JSON Object containing a property for each of the decision definitions, which are successfully deployed with that deployment. The key is the decision definition id, the value is a JSON Object corresponding to the decision definition.",
      "additionalProperties": {
        "$ref": "#/components/schemas/DecisionDefinitionDto"
      }
    },
    "deployedDecisionRequirementsDefinitions": {
      "type": "object",
      "description": "A JSON Object containing a property for each of the decision requirements definitions, which are successfully deployed with that deployment. The key is the decision requirements definition id, the value is a JSON Object corresponding to the decision requirements definition.",
      "additionalProperties": {
        "$ref": "#/components/schemas/DecisionRequirementsDefinitionDto"
      }
    },
    "deployedCaseDefinitions": {
      "type": "object",
      "description": "A JSON Object containing a property for each of the case definitions, which are successfully deployed with that deployment. The key is the case definition id, the value is a JSON Object corresponding to the case definition.",
      "additionalProperties": {
        "$ref": "#/components/schemas/CaseDefinitionDto"
      }
    }
  }
}