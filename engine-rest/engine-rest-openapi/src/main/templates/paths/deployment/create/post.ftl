{
  "operationId": "createDeployment",
  "tags": [
    "Deployment"
  ],
  "description": "Creates a deployment. Security Consideration
Deployments can contain custom code in form of scripts or EL expressions to customize process behavior. This may be abused for remote execution of arbitrary code.",
  "requestBody": {
    "content": {
      "multipart/form-data": {
        "schema": {
          "type": "object",
          "properties": {
            "tenant-id": {
              "type": "string",
              "description": "The tenant id for the deployment to be created."
            },
            "deployment-source": {
              "type": "string",
              "description": "The source for the deployment to be created."
            },
            "deploy-changed-only": {
              "type": "boolean",
              "default": false,
              "description": "A flag indicating whether the process engine should perform duplicate checking on a per-resource basis. If set to true, only those resources that have actually changed are deployed. Checks are made against resources included previous deployments of the same name and only against the latest versions of those resources. If set to true, the option enable-duplicate-filtering is overridden and set to true."
            },
            "enable-duplicate-filtering": {
              "type": "boolean",
              "default": false,
              "description": "A flag indicating whether the process engine should perform duplicate checking for the deployment or not. This allows you to check if a deployment with the same name and the same resouces already exists and if true, not create a new deployment but instead return the existing deployment. The default value is false."
            },
            "deployment-name": {
              "type": "string",
              "description": "The name for the deployment to be created."
            },
            "data": {
              "type": "string",
              "format": "binary",
              "description": "The binary data to create the deployment resource. It is possible to have more than one form part with different form part names for the binary data to create a deployment."
            }
          }
        }
      }
    }
  },
  "responses": {
    "200": {
      "description": "default response",
      "content": {
        "application/json": {
          "schema": {
            "$ref": "#/components/schemas/DeploymentDto"
          }
        }
      }
    },
    "400": {
      "description": "Bad Request",
      "content": {
        "application/json": {
          "schema": {
            "type": "object",
            "properties": {
              "type": {
                "type": "string"
              },
              "message": {
                "type": "string"
              },
              "details": {
                "type": "object",
                "properties": {
                  "{sourceName}": {
                    "type": "object",
                    "properties": {
                      "errors": {
                        "type": "object",
                        "properties": {
                          "message": {
                            "type": "string"
                          },
                          "line": {
                            "type": "integer",
                            "format": "int32"
                          },
                          "column": {
                            "type": "integer",
                            "format": "int32"
                          },
                          "mainBpmnElementId": {
                            "type": "string"
                          },
                          "bpmnElementIds": {
                            "type": "array",
                            "items": {
                              "type": "string"
                            }
                          }
                        }
                      },
                      "warnings": {
                        "type": "object"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}