<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/identity-links/get-identity-link-query/index.html -->

<#assign sortByValues = [
  '"time"',
  '"type"',
  '"userId"',
  '"groupId"',
  '"taskId"',
  '"processDefinitionId"',
  '"processDefinitionKey"',
  '"operationType"',
  '"assignerId"',
  '"tenantId"'
]>

<#assign params = {
  "type": {
    "type": "string",
    "desc": "Restricts to identity links that have the given type (candidate/assignee/owner)."
  },
  "userId": {
    "type": "string",
    "desc": "Restricts to identity links that have the given user id."
  },
  "groupId": {
    "type": "string",
    "desc": "Restricts to identity links that have the given group id."
  },
  "dateBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restricts to identity links that have the time before the given time."
  },
  "dateAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restricts to identity links that have the time after the given time."
  },
  "taskId": {
    "type": "string",
    "desc": "Restricts to identity links that have the given task id."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Restricts to identity links that have the given process definition id."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Restricts to identity links that have the given process definition key."
  },
  "operationType": {
    "type": "string",
    "desc": "Restricts to identity links that have the given operationType (add/delete)."
  },
  "assignerId": {
    "type": "string",
    "desc": "Restricts to identity links that have the given assigner id."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic identity links that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  }
}>
