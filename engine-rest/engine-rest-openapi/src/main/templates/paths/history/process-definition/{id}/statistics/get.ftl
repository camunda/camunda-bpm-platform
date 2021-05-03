<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-historic-activity-statistics-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricActivityStatistics"
      tag = "Historic Process Definition"
      summary = "Get Historic Activity Statistics"
      desc = "Retrieves historic statistics of a given process definition, grouped by activities.
              These statistics include the number of running activity instances and,
              optionally, the number of canceled activity instances, finished
              activity instances and activity instances which completed a scope
              (i.e., in BPMN 2.0 manner: a scope is completed by an activity
              instance when the activity instance consumed a token but did not emit
              a new token).
              **Note:** This only includes historic data."
  />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process definition."
    />

    <#include "/lib/commons/history-process-definition-activity-statistics.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#assign last = true >
    <#include "/lib/commons/sort-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricActivityStatisticsDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Request With `canceled=true`",
                       "description": "GET `history/process-definition/aProcessDefinitionId/statistics?canceled=true`",
                       "value": [
                         {
                           "id": "anActivity",
                           "instances": 123,
                           "canceled": 50,
                           "finished": 0,
                           "completeScope": 0,
                           "openIncidents": 0,
                           "resolvedIncidents": 0,
                           "deletedIncidents": 0
                         },
                         {
                           "id": "anotherActivity",
                           "instances": 200,
                           "canceled": 150,
                           "finished": 0,
                           "completeScope": 0,
                           "openIncidents": 0,
                           "resolvedIncidents": 0,
                           "deletedIncidents": 0
                         }
                       ]
                     }',
                     '"example-2": {
                       "summary": "Request With `finished=true`",
                       "description": "GET `history/process-definition/aProcessDefinitionId/statistics?finished=true`",
                       "value": [
                         {
                           "id": "anActivity",
                           "instances": 123,
                           "canceled": 0,
                           "finished": 20,
                           "completeScope": 0,
                           "openIncidents": 0,
                           "resolvedIncidents": 0,
                           "deletedIncidents": 0
                         },
                         {
                           "id":"anotherActivity",
                           "instances": 200,
                           "canceled": 0,
                           "finished": 30,
                           "completeScope": 0,
                           "openIncidents": 0,
                           "resolvedIncidents": 0,
                           "deletedIncidents": 0
                         }
                       ]
                     }',
                     '"example-3": {
                       "summary": "Request With `completeScope=true`",
                       "description": "GET `history/process-definition/aProcessDefinitionId/statistics?completeScope=true`",
                       "value": [
                         {
                           "id": "anActivity",
                           "instances": 123,
                           "canceled": 0,
                           "finished": 0,
                           "completeScope": 20,
                           "openIncidents": 0,
                           "resolvedIncidents": 0,
                           "deletedIncidents": 0
                         },
                         {
                           "id":"anotherActivity",
                           "instances": 200,
                           "canceled": 0,
                           "finished": 0,
                           "completeScope": 1,
                           "openIncidents": 0,
                           "resolvedIncidents": 0,
                           "deletedIncidents": 0
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>