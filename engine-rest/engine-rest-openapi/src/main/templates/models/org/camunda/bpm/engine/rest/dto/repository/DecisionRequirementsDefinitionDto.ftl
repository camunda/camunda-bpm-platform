      {
        "type": "object",
        "properties": {
          "id": {
            "type": "string",
            "description": "The id of the decision requirements definition."
          },
          "key": {
            "type": "string",
            "description": "The key of the decision requirements definition, i.e., the id of the DMN 1.1 XML decision definition."
          },
          "category": {
            "type": "string",
            "description": "The category of the decision requirements definition."
          },
          "name": {
            "type": "string",
            "description": "The name of the decision requirements definition."
          },
          "version": {
            "type": "integer",
            "format": "int32",
            "description": "The version of the decision requirements definition that the engine assigned to it."
          },
          "resource": {
            "type": "string",
            "description": "The file name of the decision requirements definition."
          },
          "deploymentId": {
            "type": "string",
            "description": "The deployment id of the decision requirements definition."
          },
          "tenantId": {
            "type": "string",
            "description": "The tenant id of the decision requirements definition."
          }
        }
      }