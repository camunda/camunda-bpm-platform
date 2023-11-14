<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricIncidents"
      tag = "Historic Incident"
      summary = "Get Incidents"
      desc = "Queries for historic incidents that fulfill given parameters.
              The size of the result set can be retrieved by using the
              [Get Incident Count](${docsUrl}/reference/rest/history/incident/get-incident-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/historic-incident-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricIncidentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/incident?processInstanceId=aProcInstId`",
                       "description": "GET `/history/incident?processInstanceId=aProcInstId`",
                       "value": [
                         {
                           "id": "anIncidentId",
                           "processDefinitionId": "aProcDefId",
                           "processInstanceId": "aProcInstId",
                           "executionId": "anExecutionId",
                           "createTime": "2014-03-01T08:00:00.000+0200",
                           "endTime": null,
                           "incidentType": "failedJob",
                           "activityId": "serviceTask",
                           "failedActivityId": "serviceTask",
                           "causeIncidentId": "aCauseIncidentId",
                           "rootCauseIncidentId": "aRootCauseIncidentId",
                           "configuration": "aConfiguration",
                           "incidentMessage": "anIncidentMessage",
                           "tenantId": null,
                           "jobDefinitionId": "aJobDefinitionId",
                           "open": true,
                           "deleted": false,
                           "resolved": false,
                           "removalTime": null,
                           "rootProcessInstanceId": "aRootProcessInstanceId",
                           "annotation": "an annotation"
                         },
                         {
                           "id": "anIncidentId",
                           "processDefinitionId": "aProcDefId",
                           "processInstanceId": "aProcInstId",
                           "executionId": "anotherExecutionId",
                           "createTime": "2014-03-01T08:00:00.000+0200",
                           "endTime": "2014-03-10T12:00:00.000+0200",
                           "incidentType": "customIncidentType",
                           "activityId": "userTask",
                           "failedActivityId": "userTask",
                           "causeIncidentId": "anotherCauseIncidentId",
                           "rootCauseIncidentId": "anotherRootCauseIncidentId",
                           "configuration": "anotherConfiguration",
                           "incidentMessage": "anotherIncidentMessage",
                           "tenantId": null,
                           "jobDefinitionId": null,
                           "open": false,
                           "deleted": false,
                           "resolved": true,
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId",
                           "annotation": "another annotation"
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>