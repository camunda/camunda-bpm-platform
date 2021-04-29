<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/get-job-log-query/index.html -->

<#assign sortByValues = [
  '"timestamp"',
  '"jobId"',
  '"jobDefinitionId"',
  '"jobDueDate"',
  '"jobRetries"',
  '"jobPriority"',
  '"activityId"',
  '"executionId"',
  '"processInstanceId"',
  '"processDefinitionId"',
  '"processDefinitionKey"',
  '"deploymentId"',
  '"hostname"',
  '"occurrence"',
  '"tenantId"'
]>
            
<#assign params = {
  "logId": {
    "type": "string",
    "desc": "Filter by historic job log id."
  },
  "jobId": {
    "type": "string",
    "desc": "Filter by job id."
  },
  "jobExceptionMessage": {
    "type": "string",
    "desc": "Filter by job exception message."
  },
  "jobDefinitionId": {
    "type": "string",
    "desc": "Filter by job definition id."
  },
  "jobDefinitionType": {
    "type": "string",
    "desc": "Filter by job definition type. See the
             [User Guide](${docsUrl}/user-guide/process-engine/the-job-executor/#job-creation)
             for more information about job definition types."
  },
  "jobDefinitionConfiguration": {
    "type": "string",
    "desc": "Filter by job definition configuration."
  },
  "activityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic job logs which belong to one of the passed activity ids."
  },
  "failedActivityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic job logs which belong to failures of one of the passed activity ids."
  },
  "executionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic job logs which belong to one of the passed execution ids."
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
  "deploymentId": {
    "type": "string",
    "desc": "Filter by deployment id."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic job log entries which belong to one of the passed and comma-
             separated tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic job log entries that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  },
  "hostname": {
    "type": "string",
    "desc": "Filter by hostname."
  },
  "jobPriorityLowerThanOrEquals": {
    "type": "integer",
    "format": "int64",
    "desc": "Only include logs for which the associated job had a priority lower than or equal to the
             given value. Value must be a valid `long` value."
  },
  "jobPriorityHigherThanOrEquals": {
  "type": "integer",
  "format": "int64",
    "desc": "Only include logs for which the associated job had a priority higher than or equal to the
             given value. Value must be a valid `long` value."
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
