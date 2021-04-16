<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getIncident"
      tag = "Incident"
      summary = "Get Incident"
      desc = "Retrieves an incident by ID." />

  "parameters": [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the incident to be retrieved." />
  ],
  "responses" : {

    <@lib.response
        code = "200"
        dto = "IncidentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/incident/anIncidentId`",
                       "value": {
                           "id": "anIncidentId",
                           "processDefinitionId": "aProcDefId",
                           "processInstanceId": "aProcInstId",
                           "executionId": "anExecutionId",
                           "incidentTimestamp": "2014-03-01T08:00:00.000+0200",
                           "incidentType": "failedJob",
                           "activityId": "serviceTask",
                           "failedActivityId": "serviceTask",
                           "causeIncidentId": "aCauseIncidentId",
                           "rootCauseIncidentId": "aRootCauseIncidentId",
                           "configuration": "aConfiguration",
                           "tenantId": null,
                           "incidentMessage": "anIncidentMessage",
                           "jobDefinitionId": "aJobDefinitionId",
                            "annotation": "an annotation"
                         }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if an incident with given id does not exist." />
    }
}

</#macro>