<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getIncidents"
      tag = "Incident"
      summary = "Get List"
      desc = "Queries for incidents that fulfill given parameters. The size of the result set can be retrieved by using
              the [Get Incident Count](${docsUrl}/reference/rest/incident/get-query-count/) method." />

  "parameters": [
    <#assign last = false >
    <#include "/lib/commons/incident-query-params.ftl">

    <#assign sortByValues = [ '"incidentId"', '"incidentMessage"', '"incidentTimestamp"', '"incidentType"',
                              '"executionId"', '"activityId"', '"processInstanceId"', '"processDefinitionId"',
                              '"causeIncidentId"', '"rootCauseIncidentId"', '"configuration"', '"tenantId"' ] >
    <#include "/lib/commons/sort-params.ftl" >
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">
  ],
  "responses" : {

    <@lib.response
        code = "200"
        dto = "IncidentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/incident/anIncidentId`",
                       "value": [
                           {
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
                           },
                           {
                             "id": "anIncidentId",
                             "processDefinitionId": "aProcDefId",
                             "processInstanceId": "aProcInstId",
                             "executionId": "anotherExecutionId",
                             "incidentTimestamp": "2014-03-01T09:00:00.000+0200",
                             "incidentType": "customIncidentType",
                             "activityId": "userTask",
                             "failedActivityId": "userTask",
                             "causeIncidentId": "anotherCauseIncidentId",
                             "rootCauseIncidentId": "anotherRootCauseIncidentId",
                             "configuration": "anotherConfiguration",
                             "tenantId": null,
                             "incidentMessage": "anotherIncidentMessage",
                             "jobDefinitionId": null,
                             "annotation": "another annotation"
                           }
                         ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy`. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error
                response format." />
    }
}

</#macro>