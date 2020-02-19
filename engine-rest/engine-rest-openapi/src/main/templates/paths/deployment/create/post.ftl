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

            <@lib.property
                name = "tenant-id"
                type = "string"
                description = "The id of the activity instance." />

            <@lib.property
                name = "deployment-source"
                type = "string"
                description = "The source for the deployment to be created." />

            <@lib.property
                name = "deploy-changed-only"
                type = "boolean"
                defaultValue = 'false'                description = "A flag indicating whether the process engine should perform duplicate checking on a per-resource basis. If set to true, only those resources that have actually changed are deployed. Checks are made against resources included previous deployments of the same name and only against the latest versions of those resources. If set to true, the option enable-duplicate-filtering is overridden and set to true." />

            <@lib.property
                name = "enable-duplicate-filtering"
                type = "boolean"
                defaultValue = 'false'              description = "A flag indicating whether the process engine should perform duplicate checking for the deployment or not. This allows you to check if a deployment with the same name and the same resouces already exists and if true, not create a new deployment but instead return the existing deployment. The default value is false." />

            <@lib.property
                name = "deployment-name"
                type = "string"
                description = "The name for the deployment to be created."/>

            <@lib.property
                name = "data"
                type = "string"
                format = "binary"
                last = true
                description = "The binary data to create the deployment resource. It is possible to have more than one form part with different form part names for the binary data to create a deployment."/>

          }
        }
      }
    }
  },
  "responses": {
    <@lib.response
        code = "200"
        dto = "DeploymentDto"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ParseExceptionDto"
        last = true
        description = "Bad Request\n
        In case one of the bpmn resources cannot be parsed."/>

  }
}