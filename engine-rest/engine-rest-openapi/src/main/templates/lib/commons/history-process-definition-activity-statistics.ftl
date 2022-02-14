<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-historic-activity-statistics-query/index.html -->

<#assign sortByValues = [
  '"activityId"'
]>

<#assign defaultDateFormat = "By [default](${docsUrl}/reference/rest/overview/date-format/), 
                              the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, 
                              e.g., `2013-01-23T14:42:45.000+0200`." >

<#assign params = {
  "canceled": {
    "type": "boolean",
    "desc": "Whether to include the number of canceled activity instances in the result or not. Valid
             values are `true` or `false`. Default: `false`."
  },
  "finished": {
    "type": "boolean",
    "desc": "Whether to include the number of finished activity instances in the result or not. Valid
             values are `true` or `false`. Default: `false`."
  },
  "completeScope": {
    "type": "boolean",
    "desc": "Whether to include the number of activity instances which completed a scope in the result
             or not. Valid values are `true` or `false`. Default: `false`."
  },
  "incidents": {
    "type": "boolean",
    "desc": "Whether to include the number of incidents. Valid values are `true` or `false`. Default: `false`."
  },
  "startedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to process instances that were started before the given date. ${defaultDateFormat}"
  },
  "startedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to process instances that were started after the given date. ${defaultDateFormat}"
  },
  "finishedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to process instances that were finished before the given date. ${defaultDateFormat}"
  },
  "finishedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to process instances that were finished after the given date. ${defaultDateFormat}"
  },
  "processInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restrict to process instances with the given IDs. The IDs must be provided as a comma-
             separated list."
  }
}>
