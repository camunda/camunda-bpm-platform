<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getMetrics"
      tag = "Metrics"
      summary = "Get Sum"
      desc = "Retrieves the `sum` (count) for a given metric." />

  "parameters" : [

    <@lib.parameter
        name = "metrics-name"
        location = "path"
        required = true
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
                "unique-task-workers",
                "process-instances",
                "task-users"]
        desc = "The name of the metric." />

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
        dto = "MetricsResultDto"
        last = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /metrics/activity-instance-end/sum?startDate=2015-01-01T00:00:00.000%2b0200",
                       "value": { "result": 4342343241 }
                     }']/>
  }
}

</#macro>