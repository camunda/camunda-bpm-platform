<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/get-external-task-log-query/index.html -->

<#assign sortByValues = [
  '"timestamp"',
  '"externalTaskId"',
  '"topicName"',
  '"workerId"',
  '"retries"',
  '"priority"',
  '"activityId"',
  '"activityInstanceId"',
  '"executionId"',
  '"processInstanceId"',
  '"processDefinitionId"',
  '"processDefinitionKey"',
  '"tenantId"'
]>
            
<#assign params = {
  "logId": {
    "type": "string",
    "desc": "Filter by historic external task log id."
  },
  "externalTaskId": {
    "type": "string",
    "desc": "Filter by external task id."
  },
  "topicName": {
    "type": "string",
    "desc": "Filter by an external task topic."
  },
  "workerId": {
    "type": "string",
    "desc": "Filter by the id of the worker that the task was most recently locked by."
  },
  "errorMessage": {
    "type": "string",
    "desc": "Filter by external task exception message."
  },
  "activityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic external task logs which belong to one of the passed activity ids."
  },
  "activityInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic external task logs which belong to one of the passed activity
             instance ids."
  },
  "executionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic external task logs which belong to one of the passed execution ids."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by process instance id."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by process definition id."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by process definition key."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic external task log entries which belong to one of the passed and
             comma-separated tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic external task log entries that belong to no tenant. Value may only
             be `true`, as `false` is the default behavior."
  },
  "priorityLowerThanOrEquals": {
    "type": "integer",
    "format": "int64",
    "desc": "Only include logs for which the associated external task had a priority lower than or
             equal to the given value. Value must be a valid `long` value."
  },
  "priorityHigherThanOrEquals": {
    "type": "integer",
    "format": "int64",
    "desc": "Only include logs for which the associated external task had a priority higher than or
             equal to the given value. Value must be a valid `long` value."
  },
  "creationLog": {
    "type": "boolean",
    "desc": "Only include creation logs. Value may only be `true`, as `false` is the default behavior."
  },
  "failureLog": {
    "type": "boolean",
    "desc": "Only include failure logs. Value may only be `true`, as `false` is the default behavior."
  },
  "successLog": {
    "type": "boolean",
    "desc": "Only include success logs. Value may only be `true`, as `false` is the default behavior."
  },
  "deletionLog": {
    "type": "boolean",
    "desc": "Only include deletion logs. Value may only be `true`, as `false` is the default behavior."
  }
}>
