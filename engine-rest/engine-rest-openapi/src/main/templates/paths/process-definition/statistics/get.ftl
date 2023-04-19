<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getProcessDefinitionStatistics"
      tag = "Process Definition"
      summary = "Get Process Instance Statistics"
      desc = "Retrieves runtime statistics of the process engine, grouped by process definitions.
              These statistics include the number of running process instances, optionally the number of failed jobs
              and also optionally the number of incidents either grouped by incident types or
              for a specific incident type.
              **Note**: This does not include historic data." />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/statistics-query-params.ftl" >

    <@lib.parameter
        name = "rootIncidents"
        location = "query"
        type = "boolean"
        last = true
        desc = "Valid values for this property are `true` or `false`.
                If this property has been set to `true` the result will include the corresponding number of
                root incidents for each occurred incident type.
                If it is set to `false`, the incidents will not be included in the result.
                Cannot be used in combination with `incidentsForType` or `incidents`."/>

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessDefinitionStatisticsResultDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /process-definition/statistics?failedJobs=true",
                       "description": "Request with Query Parameter `failedJobs=true`",
                       "value": [{
                         "@class": "org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto",
                         "id":"aProcessDefinitionId",
                         "instances": 123,
                         "failedJobs": 42,
                         "definition": 
                           {"id": "aProcessDefinitionId",
                           "key": "aKey",
                           "category": null,
                           "description": null,
                           "name": "aName",
                           "version": 0,
                           "resource": null,
                           "deploymentId": null,
                           "diagram": null,
                           "suspended": false,
                           "tenantId": null,
                           "versionTag": "1.0.0",
                           "historyTimeToLive": null,
                           "startableInTasklist": false},
                         "incidents": []
                        },
                        {
                         "@class": "org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto",
                         "id": "aProcessDefinitionId:2",
                         "instances": 124,
                         "failedJobs": 43,
                         "definition": 
                           {"id": "aProcessDefinitionId:2",
                           "key": "aKey",
                           "category": null,
                           "description": null,
                           "name": "aName",
                           "version": 0,
                           "resource": null,
                           "deploymentId": null,
                           "diagram": null,
                           "suspended": false,
                           "tenantId": null,
                           "versionTag": null,
                           "historyTimeToLive": null,
                           "startableInTasklist": false},
                         "incidents": []
                       }]
                    }',
                    '"example-2": {
                       "summary": "GET /process-definition/statistics?incidents=true",
                       "description": "Request with Query Parameter `incidents=true`",
                       "value": [{
                         "@class": "org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto",
                         "id": "aProcessDefinitionId",
                         "instances": 123,
                         "failedJobs": 0,
                         "definition": {
                           "id": "aProcessDefinitionId",
                           "key": "aKey",
                           "category": null,
                           "description": null,
                           "name": "aName",
                           "version": 0,
                           "resource": null,
                           "deploymentId": null,
                           "diagram": null,
                           "suspended": false,
                           "tenantId": null,
                           "versionTag": "1.0.0",
                           "historyTimeToLive": null,
                           "startableInTasklist": false
                         },
                         "incidents": [{
                           "incidentType": "failedJob",
                           "incidentCount": 42
                         }, {
                           "incidentType": "anIncident",
                           "incidentCount": 20
                         }]
                     }, {
                         "@class": "org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto",
                         "id": "aProcessDefinitionId:2",
                         "instances": 124,
                         "failedJobs": 0,
                         "definition": {
                           "id": "aProcessDefinitionId:2",
                           "key": "aKey",
                           "category": null,
                           "description": null,
                           "name": "aName",
                           "version": 0,
                           "resource": null,
                           "deploymentId": null,
                           "diagram": null,
                           "suspended": false,
                           "tenantId": null,
                           "versionTag": null,
                           "historyTimeToLive": null,
                           "startableInTasklist": false
                         },
                         "incidents": [{
                           "incidentType": "failedJob",
                           "incidentCount": 43
                         }, {
                           "incidentType": "anIncident",
                           "incidentCount": 22
                         }, {
                           "incidentType": "anotherIncident",
                           "incidentCount": 15
                         }]
                     }]
                     }',
                    '"example-3": {
                       "summary": "GET /process-definition/statistics?incidentsForType=anIncident",
                       "description": "Request with Query Parameter `incidentsForType=anIncident`",
                       "value": [{
                         "@class": "org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto",
                         "id":"aProcessDefinitionId",
                         "id": "aProcessDefinitionId",
                         "instances": 123,
                         "failedJobs": 0,
                         "definition": {
                             "id": "aProcessDefinitionId",
                             "key": "aKey",
                             "category": null,
                             "description": null,
                             "name": "aName",
                             "version": 0,
                             "resource": null,
                             "deploymentId": null,
                             "diagram": null,
                             "suspended": false,
                             "tenantId": null,
                             "versionTag": "1.0.0",
                             "historyTimeToLive": null,
                             "startableInTasklist": false
                         },
                         "incidents" : [{
                           "incidentType": "anIncident",
                           "incidentCount": 20
                         }]
                        }]
                     }',
                    '"example-4": {
                       "summary": "GET /process-definition/statistics?rootIncidents=true",
                       "description": "Request with Query Parameter `rootIncidents=true`",
                       "value": [{
                         "@class": "org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto",
                         "id": "aProcessDefinitionId",
                         "instances": 123,
                         "failedJobs": 0,
                         "definition": {
                             "id": "aProcessDefinitionId",
                             "key": "aKey",
                             "category": null,
                             "description": null,
                             "name": "aName",
                             "version": 0,
                             "resource": null,
                             "deploymentId": null,
                             "diagram": null,
                             "suspended": false,
                             "tenantId": null,
                             "versionTag": "1.0.0",
                             "historyTimeToLive": null,
                             "startableInTasklist": false
                         },
                         "incidents": [{
                           "incidentType": "failedJob",
                           "incidentCount": 62
                         }, {
                           "incidentType": "anIncident",
                           "incidentCount": 20
                             }]
                        }]
                     }'
                   ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>
