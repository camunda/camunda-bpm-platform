<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getActivityStatisticsByProcessDefinitionKeyAndTenantId"
      tag = "Process Definition"
      summary = "Get Activity Instance Statistics"
      desc = "Retrieves runtime statistics of the latest version of the given process definition for a tenant,
              grouped by activities.
              These statistics include the number of running activity instances, optionally the number of failed jobs
              and also optionally the number of incidents either grouped by incident types or
              for a specific incident type.
              **Note**: This does not include historic data." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the process definition (the latest version thereof) to be retrieved."/>

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the tenant the process definition belongs to."/>

    <#assign last = true >
    <#include "/lib/commons/statistics-query-params.ftl" >
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ActivityStatisticsResultDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /process-definition/key/aProcessDefinitionKey/tenant-id/aTenantId/statistics?failedJobs=true",
                       "description": "Request with Query Parameter `failedJobs=true`",
                       "value": [{
                         "id":"anActivity",
                         "instances": 123,
                         "failedJobs": 42,
                         "incidents": []
                        },
                        {"id": "anotherActivity",
                         "instances": 124,
                         "failedJobs": 43,
                         "incidents": []
                       }]
                    }',
                    '"example-2": {
                       "summary": "GET /process-definition/key/aProcessDefinitionKey/tenant-id/aTenantId/statistics?incidents=true",
                       "description": "Request with Query Parameter `incidents=true`",
                       "value": [{
                         "id": "anActivity",
                         "instances": 123,
                         "failedJobs": 0,
                         "incidents": [{
                           "incidentType": "failedJob",
                           "incidentCount": 42
                         }, {
                           "incidentType": "anIncident",
                           "incidentCount": 20
                         }]
                     }, {
                         "id": "anotherActivity",
                         "instances": 124,
                         "failedJobs": 0,
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
                       "summary": "GET /process-definition/key/aProcessDefinitionKey/tenant-id/aTenantId/statistics?incidentsForType=anIncident",
                       "description": "Request with Query Parameter `incidentsForType=anIncident`",
                       "value": [{
                         "id": "anActivity",
                         "instances": 123,
                         "failedJobs": 0,
                         "incidents" : [{
                           "incidentType": "anIncident",
                           "incidentCount": 20
                         }]
                        }]
                     }'
                   ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given key does not exist.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />


  }
}

</#macro>