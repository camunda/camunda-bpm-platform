{
  "operationId": "createDeployment",
  "tags": [
    "Deployment"
  ],
  "description": "Creates a deployment.

                  **Security Consideration**

                  Deployments can contain custom code in form of scripts or EL expressions to customize process behavior.
                  This may be abused for remote execution of arbitrary code.",

  <@lib.requestBody
      mediaType = "multipart/form-data"
      dto = "MultiFormDeploymentDto" />

  "responses": {
    <@lib.response
        code = "200"
        dto = "DeploymentDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ParseExceptionDto"
        last = true
        desc = "Bad Request
                In case one of the bpmn resources cannot be parsed."/>

  }
}