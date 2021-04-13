<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-create-incident/index.html -->
{
  <@lib.endpointInfo
      id = "createIncident"
      tag = "Execution"
      summary = "Create Incident"
      desc = "Creates a custom incident with given properties."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to create a new incident for."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CreateIncidentDto"
      examples = ['"example-1": {
                     "summary": "POST `/execution/anExecutionId/create-incident`",
                     "value": {
                       "incidentType": "aType",
                       "configuration": "aConfiguration"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "IncidentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/execution/anExecutionId/create-incident`",
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
                         "jobDefinitionId": "aJobDefinitionId"
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if the incident type is null, the execution does not exist or the
                execution is not related to any activity."
        last = true
    />

  }

}

</#macro>