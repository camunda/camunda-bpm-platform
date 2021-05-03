<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->

<#assign sortByValues = [
  '"incidentId"',
  '"incidentMessage"',
  '"createTime"',
  '"endTime"',
  '"incidentType"',
  '"executionId"',
  '"activityId"',
  '"processInstanceId"',
  '"processDefinitionId"',
  '"processDefinitionKey"',
  '"causeIncidentId"',
  '"rootCauseIncidentId"',
  '"configuration"',
  '"historyConfiguration"',
  '"tenantId"',
  '"incidentState"'
]>

<#assign dateDefault = "By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
                        `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`.">

<#assign params = {
  "incidentId": {
    "type": "string",
    "desc": "Restricts to incidents that have the given id."
  },
  "incidentType": {
    "type": "string",
    "desc": "Restricts to incidents that belong to the given incident type. See the [User
             Guide](/manual/develop/user-guide/process-engine/incidents/#incident-types)
             for a list of incident types."
  },
  "incidentMessage": {
    "type": "string",
    "desc": "Restricts to incidents that have the given incident message."
  },
  "incidentMessageLike": {
    "type": "string",
    "desc": "Restricts to incidents that incidents message is a substring of the given value.
                  The string can include the wildcard character '%' to express
                  like-strategy: starts with (string%), ends with (%string) or contains
             (%string%).
"
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Restricts to incidents that belong to a process definition with the given id."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Restricts to incidents that have the given processDefinitionKey."
  },
  "processDefinitionKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restricts to incidents that have one of the given process definition keys."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Restricts to incidents that belong to a process instance with the given id."
  },
  "executionId": {
    "type": "string",
    "desc": "Restricts to incidents that belong to an execution with the given id."
  },
  "createTimeBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restricts to incidents that have a createTime date before the given date. ${dateDefault}"
  },
  "createTimeAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restricts to incidents that have a createTime date after the given date. ${dateDefault}"
  },
  "endTimeBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restricts to incidents that have an endTimeBefore date before the given date. ${dateDefault}"
  },
  "endTimeAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restricts to incidents that have an endTimeAfter date after the given date. ${dateDefault}"
  },
  "activityId": {
    "type": "string",
    "desc": "Restricts to incidents that belong to an activity with the given id."
  },
  "failedActivityId": {
    "type": "string",
    "desc": "Restricts to incidents that were created due to the failure of an activity with the given
             id."
  },
  "causeIncidentId": {
    "type": "string",
    "desc": "Restricts to incidents that have the given incident id as cause incident."
  },
  "rootCauseIncidentId": {
    "type": "string",
    "desc": "Restricts to incidents that have the given incident id as root cause incident."
  },
  "configuration": {
    "type": "string",
    "desc": "Restricts to incidents that have the given parameter set as configuration."
  },
  "historyConfiguration": {
    "type": "string",
    "desc": "Restricts to incidents that have the given parameter set as history configuration."
  },
  "open": {
    "type": "boolean",
    "desc": "Restricts to incidents that are open."
  },
  "resolved": {
    "type": "boolean",
    "desc": "Restricts to incidents that are resolved."
  },
  "deleted": {
    "type": "boolean",
    "desc": "Restricts to incidents that are deleted."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restricts to incidents that have one of the given comma-separated tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic incidents that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  },
  "jobDefinitionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restricts to incidents that have one of the given comma-separated job definition ids."
  }
}>
