<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getDeployment"
      tag = "Deployment"
      summary = "Get"
      desc = "Retrieves a deployment by id, according to the `Deployment` interface of the engine." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the deployment." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "DeploymentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/deployments/someDeploymentId`",
                       "value": {
                         "id": "someDeploymentId",
                         "name": "deploymentName",
                         "source": "process application",
                         "deploymentTime": "2013-04-23T13:42:43.000+0200"
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Deployment with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>