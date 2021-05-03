<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-cleanable-process-instance-report-query/index.html -->

<#assign sortByValues = [
  '"finished"'
]>

<#assign params = {
  "processDefinitionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by process definition ids. Must be a comma-separated list of process definition ids."
  },
  "processDefinitionKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by process definition keys. Must be a comma-separated list of process definition keys."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of tenant ids. A process definition must have one of the given 
             tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include process definitions which belong to no tenant. Value may only be `true`, as
             `false` is the default behavior."
  },
  "compact": {
    "type": "boolean",
    "desc": "Only include process instances which have more than zero finished instances. Value may
             only be `true`, as `false` is the default behavior."
  }
}>
