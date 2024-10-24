<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "interval"
      tag = "Metrics"
      summary = "Get Metrics in Interval"
      desc = "Retrieves a list of metrics, aggregated for a given interval." />

  "parameters" : [

    <@lib.parameter
        name = "name"
        location = "query"
        type = "string"
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
                "process-instances"]
        desc = "The name of the metric." />

    <@lib.parameter
        name = "reporter"
        location = "query"
        type = "string"
        desc = "The name of the reporter (host), on which the metrics was logged. This will have
                value provided by the [hostname configuration property](${docsUrl}/reference/deployment-descriptors/tags/process-engine/#hostname)." />

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
        desc = "The end date (exclusive)."/>

    <#assign last = false />
    <#include "/lib/commons/pagination-params.ftl" >

    <@lib.parameter
        name = "interval"
        location = "query"
        type = "integer"
        format = "int64"
        defaultValue = "900"
        desc = "The interval for which the metrics should be aggregated. Time unit is seconds.
                Default: The interval is set to 15 minutes (900 seconds)." />

    <@lib.parameter
        name = "aggregateByReporter"
        location = "query"
        type = "string"
        last = true
        desc = "Aggregate metrics by reporter." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "MetricsIntervalResultDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /metrics?name=flow-node-instances&startDate=1970-01-01T01:45:00.000%2b0200&endDate=1970-01-01T02:00:00.000%2b0200",
                       "value": [
                                  {
                                    "timestamp":"1970-01-01T01:45:00.000+0200",
                                    "name":"flow-node-instances",
                                    "reporter":"REPORTER",
                                    "value":23
                                  }
                                ]
                     }']/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid." />
  }
}

</#macro>