<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->

<#assign sortByValues = [
  '"evaluationTime"',
  '"tenantId"'
]>

<#assign params = {
  "decisionInstanceId": {
    "type": "string",
    "desc": "Filter by decision instance id."
  },
  "decisionInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by decision instance ids. Must be a comma-separated list of decision instance ids."
  },
  "decisionDefinitionId": {
    "type": "string",
    "desc": "Filter by the decision definition the instances belongs to."
  },
  "decisionDefinitionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by the decision definitions the instances belongs to. Must be a
             comma-separated list of decision definition ids."
  },
  "decisionDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the decision definition the instances belongs to."
  },
  "decisionDefinitionKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by the keys of the decision definition the instances belongs to. Must be a comma-
             separated list of decision definition keys."
  },
  "decisionDefinitionName": {
    "type": "string",
    "desc": "Filter by the name of the decision definition the instances belongs to."
  },
  "decisionDefinitionNameLike": {
    "type": "string",
    "desc": "Filter by the name of the decision definition the instances belongs to, that the parameter
             is a substring of."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by the process definition the instances belongs to."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the process definition the instances belongs to."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by the process instance the instances belongs to."
  },
  "caseDefinitionId": {
    "type": "string",
    "desc": "Filter by the case definition the instances belongs to."
  },
  "caseDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the case definition the instances belongs to."
  },
  "caseInstanceId": {
    "type": "string",
    "desc": "Filter by the case instance the instances belongs to."
  },
  "activityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by the activity ids the instances belongs to.
             Must be a comma-separated list of acitvity ids."
  },
  "activityInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by the activity instance ids the instances belongs to.
             Must be a comma-separated list of acitvity instance ids."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of tenant ids. A historic decision instance must have one
             of the given tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic decision instances that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  },
  "evaluatedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were evaluated before the given date.
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-
             dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "evaluatedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were evaluated after the given date.
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-
             dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "userId": {
    "type": "string",
    "desc": "Restrict to instances that were evaluated by the given user."
  },
  "rootDecisionInstanceId": {
    "type": "string",
    "desc": "Restrict to instances that have a given root decision instance id.
             This also includes the decision instance with the given id."
  },
  "rootDecisionInstancesOnly": {
    "type": "boolean",
    "desc": "Restrict to instances those are the root decision instance of an evaluation.
             Value may only be `true`, as `false` is the default behavior."
  },
  "decisionRequirementsDefinitionId": {
    "type": "string",
    "desc": "Filter by the decision requirements definition the instances belongs to."
  },
  "decisionRequirementsDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the decision requirements definition the instances belongs to."
  }
}>
