<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getHistoricProcessInstanceDurationReport"
      tag = "Historic Process Instance"
      summary = "Get Duration Report"
      desc = "Retrieves a report about the duration of completed process instances, grouped by a period.
              These reports include the maximum, minimum and average duration of all completed process instances which were started in a given period.

              **Note:** This only includes historic data." />

  "parameters" : [

    <@lib.parameter
        name = "reportType"
        location = "query"
        type = "string"
        required = true
        desc = "**Mandatory.** Specifies the type of the report to retrieve.
                To retrieve a report about the duration of process instances, the value must be set to `duration`." />

    <@lib.parameter
        name = "periodUnit"
        location = "query"
        type = "string"
        required = true
        enumValues = ["month", "quarter"]
        desc = "**Mandatory.** Specifies the granularity of the report. Valid values are `month` and `quarter`." />

    <@lib.parameter
        name = "processDefinitionIdIn"
        location = "query"
        type = "array"
        desc = "Filter by process definition ids. Must be a comma-separated list of process definition ids." />

    <@lib.parameter
        name = "processDefinitionKeyIn"
        location = "query"
        type = "array"
        desc = "Filter by process definition keys. Must be a comma-separated list of process definition keys." />

    <@lib.parameter
        name = "startedBefore"
        location = "query"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that were started before the given date.
                By [default](), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2016-01-23T14:42:45.000+0200`." />

    <@lib.parameter
        name = "startedAfter"
        location = "query"
        type = "string"
        format = "date-time"
        last = true
        desc = "Restrict to instances that were started after the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2016-01-23T14:42:45.000+0200`." />

  ],

  "responses" : {

    <@lib.multiTypeResponse
        code = "200"
        desc = "Request successful."
        types = [
          {
            "dto": "DurationReportResultDto",
            "array": true,
            "examples": ['"example-1": {
                            "summary": "GET `/history/process-instance/report?reportType=duration&periodUnit=quarter&processDefinitionKeyIn=invoice`",
                            "value": [
                                  {
                                    "period": 1,
                                    "periodUnit": "QUARTER",
                                    "maximum": 500000,
                                    "minimum": 250000,
                                    "average": 375000
                                  },
                                  {
                                    "period": 2,
                                    "periodUnit": "QUARTER",
                                    "maximum": 600000,
                                    "minimum": 300000,
                                    "average": 450000
                                  },
                                  {
                                    "period": 3,
                                    "periodUnit": "QUARTER",
                                    "maximum": 1000000,
                                    "minimum": 500000,
                                    "average": 750000
                                  },
                                  {
                                    "period": 4,
                                    "periodUnit": "QUARTER",
                                    "maximum": 200000,
                                    "minimum": 100000,
                                    "average": 150000
                                  }
                                ]
                          }'
                        ]
          },
          {
            "mediaType": "application/csv"
          },
          {
            "mediaType": "text/csv"
          }
        ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid or mandatory parameters are not supplied.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        last = true
        desc = "If the authenticated user is unauthorized to read the history.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}

</#macro>