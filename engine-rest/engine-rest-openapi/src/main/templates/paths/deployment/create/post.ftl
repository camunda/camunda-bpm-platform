<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "createDeployment"
      tag = "Deployment"
      summary = "Create"
      desc = "Creates a deployment.

              **Security Consideration**

              Deployments can contain custom code in form of scripts or EL expressions to customize process behavior.
              This may be abused for remote execution of arbitrary code." />

  <@lib.requestBody
      mediaType = "multipart/form-data"
      dto = "MultiFormDeploymentDto" />

  "responses": {

    <@lib.response
        code = "200"
        dto = "DeploymentWithDefinitionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                     "summary": "POST `/deployment/create`",
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
                       "source": "process application",
                       "deploymentTime": "2013-01-23T13:59:43.000+0200",
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
                     }'
                   ] />

    <@lib.response
        code = "400"
        dto = "ParseExceptionDto"
        last = true
        desc = "Bad Request. In case one of the bpmn resources cannot be parsed.

                See the [Introduction](${docsUrl}/reference/rest/overview/#parse-exceptions) for
                the error response format."
        examples = ['"example-1": {
                     "summary": "GET /deployment?name=deploymentName",
                     "value": {
                         "type": "ParseException",
                         "message": "ENGINE-09005 Could not parse BPMN process. Errors: Exclusive Gateway \'ExclusiveGateway_1\' has outgoing sequence flow \'SequenceFlow_0\' without condition which is not the default flow.",
                         "details": {
                           "invoice.bpmn": {
                             "errors": [
                               {
                                 "message": "Exclusive Gateway \'ExclusiveGateway_1\' has outgoing sequence flow \'SequenceFlow_0\' without condition which is not the default flow.",
                                 "line": 77,
                                 "column": 15,
                                 "mainBpmnElementId": "ExclusiveGateway_1",
                                 "bpmnElementIds": [
                                   "ExclusiveGateway_1",
                                   "SequenceFlow_0"
                                 ]
                               }
                             ],
                             "warnings": [
                               {
                                 "message": "It is not recommended to use a cancelling boundary timer event with a time cycle.",
                                 "line": 87,
                                 "column": 20,
                                 "mainBpmnElementId": "BoundaryEvent_1",
                                 "bpmnElementIds": [
                                   "BoundaryEvent_1"
                                 ]
                               }
                             ]
                           }
                         }
                       }
                     }' 
                   ] />

  }
}
</#macro>