<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getDeploymentResource"
      tag = "Deployment"
      summary = "Get Resource"
      desc = "Retrieves a deployment resource by resource id for the given deployment." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the deployment" />

    <@lib.parameter
        name = "resourceId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the deployment resource" />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "DeploymentResourceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/deployments/someDeploymentId/resources/someResourceId`",
                       "value": {
                          "id": "someResourceId",
                          "name": "someResourceName",
                          "deploymentId": "someDeploymentId"
                        }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Deployment Resource with given resource id or deployment id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>