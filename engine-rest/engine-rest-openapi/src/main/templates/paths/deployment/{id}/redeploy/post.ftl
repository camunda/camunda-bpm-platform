<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "redeploy"
      tag = "Deployment"
      summary = "Redeploy"
      desc = "Re-deploys an existing deployment.

              The deployment resources to re-deploy can be restricted by using the properties `resourceIds` or
              `resourceNames`. If no deployment resources to re-deploy are passed then all existing resources of the
              given deployment are re-deployed.

              **Warning**: Deployments can contain custom code in form of scripts or EL expressions to customize
              process behavior. This may be abused for remote execution of arbitrary code. See the section on
              [security considerations for custom code](${docsUrl}/user-guide/process-engine/securing-custom-code/) in
              the user guide for details." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the deployment to re-deploy." />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "RedeploymentDto"
      examples = ['"example-1": {
                       "summary": "POST `/deployment/anDeploymentId/redeploy`",
                       "value": {
                         "resourceIds": [ "aResourceId" ],
                         "resourceNames": [ "aResourceName" ],
                         "source" : "cockpit"
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "DeploymentWithDefinitionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "value": {
                         "links": [
                             {
                                 "method": "GET",
                                 "href": "http://localhost:38080/rest-test/deployment/aDeploymentId",
                                 "rel": "self"
                             }
                         ],
                         "id": "aDeploymentId",
                         "name": "aName",
                         "source": "cockpit",
                         "deploymentTime": "2015-10-13T13:59:43.000+0200",
                         "tenantId": null,
                         "deployedProcessDefinitions": {
                             "aProcDefId": {
                                 "id": "aProcDefId",
                                 "key": "aKey",
                                 "category": "aCategory",
                                 "description": "aDescription",
                                 "name": "aName",
                                 "version": 42,
                                 "resource": "aResourceName",
                                 "deploymentId": "aDeploymentId",
                                 "diagram": "aResourceName.png",
                                 "suspended": true,
                                 "tenantId": null,
                                 "versionTag": null
                             }
                         },
                         "deployedCaseDefinitions": null,
                         "deployedDecisionDefinitions": null,
                         "deployedDecisionRequirementsDefinitions": null
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Deployment or a deployment resource for the given deployment does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>