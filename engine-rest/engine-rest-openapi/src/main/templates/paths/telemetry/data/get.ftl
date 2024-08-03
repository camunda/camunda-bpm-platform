<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTelemetryData"
      tag = "Telemetry"
      summary = "Fetch Telemetry Data"
      desc = "Fetches metrics count based on the start and end date" />

    "parameters" : [

      <@lib.parameter
        name = "metricsFilter"
        location = "query"
        type = "string"
        required = false
        enumValues = ["activity-instance-start",
        "activity-instance-end",
        "decision-instances",
        "flow-node-instances",
        "job-acquisition-attempt",
        "job-acquired-success",
        "job-acquired-failure",
        "job-execution-rejected",
        "job-successful",
        "job-failed",
        "job-locked-exclusive",
        "executed-decision-elements",
        "history-cleanup-removed-process-instances",
        "history-cleanup-removed-case-instances",
        "history-cleanup-removed-decision-instances",
        "history-cleanup-removed-batch-operations",
        "history-cleanup-removed-task-metrics",
        "unique-task-workers",
        "process-instances",
        "task-users"]
        desc = "The filter of metric." />

      <@lib.parameter
        name = "startDate"
        location = "query"
        type = "string"
        format = "date-time"
        desc = "The start date (inclusive)."/>

    <@lib.parameter
      name = "endDate"
      location = "query"
      type = "string"
      format = "date-time"
      last = true
      desc = "The end date (exclusive)."/>

    ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "TelemetryDataDto"
        desc = "Request successful."
        last = true
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "The Response content of a status 200",
                       "value": {
                           "installation": "8343cc7a-8ad1-42d4-97d2-43452c0bdfa3",
                           "product": {
                             "name": "Camunda BPM Runtime",
                             "version": "7.14.0",
                             "edition": "enterprise",
                             "internals": {
                               "database": {  
                                 "vendor": "h2",
                                 "version": "1.4.190 (2015-10-11)"
                               },
                               "application-server": {
                                 "vendor": "Wildfly",
                                 "version": "WildFly Full 19.0.0.Final (WildFly Core 11.0.0.Final) - 2.0.30.Final"
                               },
                               "jdk": {
                                 "version": "14.0.2",
                                 "vendor": "Oracle Corporation"
                               },
                               "commands": {
                                 "StartProcessInstanceCmd": {"count": 40},
                                 "FetchExternalTasksCmd":  {"count": 100}
                               },
                               "metrics": {
                                 "process-instances": { "count": 936 },
                                 "flow-node-instances": { "count": 6125 },
                                 "decision-instances": { "count": 140 },
                                 "executed-decision-elements": { "count": 732 }
                               },
                               "data-collection-start-date": "2022-11-320T15:53:20.386+0100",
                               "camunda-integration": [
                                 "spring-boot-starter",
                                 "camunda-bpm-run"
                               ],
                               "license-key": {
                                 "customer": "customer name",
                                 "type": "UNIFIED",
                                 "valid-until": "2022-09-30",
                                 "unlimited": false,
                                 "features": {
                                   "camundaBPM": "true"
                                 },
                                 "raw": "customer=customer name;expiryDate=2022-09-30;camundaBPM=true;optimize=false;cawemo=false"
                               },
                               "webapps": [
                                 "cockpit",
                                 "admin"
                               ]
                             }
                           }
                       }
                     }'] />

  }
}
</#macro>