<#macro endpoint_macro docsUrl="">
{

    <@lib.endpointInfo
      id = "getRegisteredDeployments"
      tag = "Deployment"
      summary = "Get Registered Deployments"
      desc = "Queries the registered deployment IDs for the current application." />

  "responses" : {
    <@lib.response
      code = "200"
      mediaType = "application/json"
      flatType="string"
      array = true
      last = true
      desc = "Request successful."
      examples = ['"example-1": {
                     "summary": "Status 200 Response",
                     "description": "The Response content of a status 200",
                     "value": [
                       "deploymentId1",
                       "deploymentId2",
                       "deploymentId3"
                     ]
                   }'] />
  }
}

</#macro>