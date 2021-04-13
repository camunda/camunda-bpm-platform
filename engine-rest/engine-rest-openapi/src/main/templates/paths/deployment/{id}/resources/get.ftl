<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getDeploymentResources"
      tag = "Deployment"
      summary = "Get Resources"
      desc = "Retrieves all deployment resources of a given deployment." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the deployment to retrieve the deployment resources for." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "DeploymentResourceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/deployment/anDeploymentId/resources`",
                       "value": [
                         {
                           "id": "anResourceId",
                           "name": "anResourceName",
                           "deploymentId": "anDeploymentId"
                         },
                         {
                           "id": "anotherResourceId",
                           "name": "anotherResourceName",
                           "deploymentId": "anDeploymentId"
                         }
                       ]
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Deployment resources for the given deployment do not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>