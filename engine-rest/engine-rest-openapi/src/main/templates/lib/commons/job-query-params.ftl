<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/get-query/index.html -->

<#assign sortByValues = [
  '"jobId"',
  '"executionId"',
  '"processInstanceId"',
  '"processDefinitionId"',
  '"processDefinitionKey"',
  '"jobPriority"',
  '"jobRetries"',
  '"jobDueDate"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign listType = "comma-separated">
  <#assign dueDateDesc = "Due date expressions are comma-separated and are structured as follows:

                         A valid condition value has the form `operator_value`.
                         `operator` is the comparison operator to be used and `value` the date value
                         as string.

                         Valid operator values are: `gt` - greater than; `lt` - lower than.
                         `value` may not contain underscore or comma characters.">
  <#assign createTimesDesc = "
                             Create time expressions are comma-separated and are structured as
                             follows:

                             A valid condition value has the form `operator_value`.
                             `operator` is the comparison operator to be used and `value` the date value
                             as string.

                             Valid operator values are: `gt` - greater than; `lt` - lower than.
                             `value` may not contain underscore or comma characters.">

<#elseif requestMethod == "POST">
  <#assign listType = "">
  <#assign dueDateDesc = "">
  <#assign createTimesDesc = "">
</#if>

<#assign params = {
  "jobId": {
    "type": "string",
    "desc": "Filter by job id."
  },
  "jobIds": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listType} list of job ids."
  },
  "jobDefinitionId": {
    "type": "string",
    "desc": "Only select jobs which exist for the given job definition."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Only select jobs which exist for the given process instance."
  },
  "processInstanceIds": {
    "type": "array",
    "itemType": "string",
    "desc": "Only select jobs which exist for the given ${listType} list of process instance ids."
  },
  "executionId": {
    "type": "string",
    "desc": "Only select jobs which exist for the given execution."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by the id of the process definition the jobs run on."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the process definition the jobs run on."
  },
  "activityId": {
    "type": "string",
    "desc": "Only select jobs which exist for an activity with the given id."
  },
  "withRetriesLeft": {
    "type": "boolean",
    "desc": "Only select jobs which have retries left. Value may only be `true`, as `false` is the
             default behavior."
  },
  "executable": {
    "type": "boolean",
    "desc": "Only select jobs which are executable, i.e., retries > 0 and due date is `null` or due
             date is in the past. Value may only be `true`, as `false` is the default
             behavior."
  },
  "timers": {
    "type": "boolean",
    "desc": "Only select jobs that are timers. Cannot be used together with `messages`. Value may only
             be `true`, as `false` is the default behavior."
  },
  "messages": {
    "type": "boolean",
    "desc": "Only select jobs that are messages. Cannot be used together with `timers`. Value may only
             be `true`, as `false` is the default behavior."
  },
  "dueDates": {
    "type": "array",
    "dto": "JobConditionQueryParameterDto",
    "desc": "Only select jobs where the due date is lower or higher than the given date.
             ${dueDateDesc}"
  },
  "createTimes": {
    "type": "array",
    "dto": "JobConditionQueryParameterDto",
    "desc": "Only select jobs created before or after the given date.
             ${createTimesDesc}"
  },
  "withException": {
    "type": "boolean",
    "desc": "Only select jobs that failed due to an exception. Value may only be `true`, as `false` is
             the default behavior."
  },
  "exceptionMessage": {
    "type": "string",
    "desc": "Only select jobs that failed due to an exception with the given message."
  },
  "failedActivityId": {
    "type": "string",
    "desc": "Only select jobs that failed due to an exception at an activity with the given id."
  },
  "noRetriesLeft": {
    "type": "boolean",
    "desc": "Only select jobs which have no retries left. Value may only be `true`, as `false` is the
             default behavior."
  },
  "active": {
    "type": "boolean",
    "desc": "Only include active jobs. Value may only be `true`, as `false` is the default behavior."
  },
  "suspended": {
    "type": "boolean",
    "desc": "Only include suspended jobs. Value may only be `true`, as `false` is the default behavior."
  },
  "priorityLowerThanOrEquals": {
    "type": "integer",
    "format": "int64",
    "desc": "Only include jobs with a priority lower than or equal to the given value. Value must be a
             valid `long` value."
  },
  "priorityHigherThanOrEquals": {
    "type": "integer",
    "format": "int64",
    "desc": "Only include jobs with a priority higher than or equal to the given value. Value must be a
             valid `long` value."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include jobs which belong to one of the passed ${listType} tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include jobs which belong to no tenant. Value may only be `true`, as `false` is the
             default behavior."
  },
  "includeJobsWithoutTenantId": {
    "type": "boolean",
    "desc": "Include jobs which belong to no tenant. Can be used in combination with `tenantIdIn`.
             Value may only be `true`, as `false` is the default behavior."
  }
}>
