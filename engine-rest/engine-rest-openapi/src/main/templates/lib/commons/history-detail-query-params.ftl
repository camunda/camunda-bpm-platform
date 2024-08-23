<#assign sortByValues = [
  '"processInstanceId"',
  '"variableName"',
  '"variableType"',
  '"variableRevision"',
  '"formPropertyId"',
  '"time"',
  '"occurrence"',
  '"tenantId"'
]>

<#assign dateFormatDescription = "Default [format](${docsUrl}/reference/rest/overview/date-format/)
                                  `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., 2013-01-23T14:42:45.000+0200."/>
<#if requestMethod == "GET">
    <#assign listType = "comma-separated">
<#elseif requestMethod == "POST">
    <#assign listType = "">
</#if>

<#assign params = {
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by process instance id."
  },
  "processInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic details which belong to one of the passed ${listType} process instance ids."
  },
  "executionId": {
    "type": "string",
    "desc": "Filter by execution id."
  },
  "taskId": {
    "type": "string",
    "desc": "Filter by task id."
  },
  "activityInstanceId": {
    "type": "string",
    "desc": "Filter by activity instance id."
  },
  "caseInstanceId": {
    "type": "string",
    "desc": "Filter by case instance id."
  },
  "caseExecutionId": {
    "type": "string",
    "desc": "Filter by case execution id."
  },
  "variableInstanceId": {
    "type": "string",
    "desc": "Filter by variable instance id."
  },
  "variableTypeIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic details where the variable updates belong to one of the passed ${listType}
             list of variable types. A list of all supported variable types can be found
             [here](${docsUrl}/user-guide/process-engine/variables/#supported-variable-values).
             **Note:** All non-primitive variables are associated with the type `serializable`."
  },
  "variableNameLike": {
    "type": "string",
    "desc": "Filter by variable name like. Example usage: `variableNameLike(%camunda%)`. The query will match the names of variables in a case-insensitive way."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listType} list of tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic details that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  },
  "userOperationId": {
    "type": "string",
    "desc": "Filter by a user operation id."
  },
  "formFields": {
    "type": "boolean",
    "desc": "Only include `HistoricFormFields`. Value may only be `true`, as `false` is the default behavior."
  },
  "variableUpdates": {
    "type": "boolean",
    "desc": "Only include `HistoricVariableUpdates`. Value may only be `true`, as `false` is the default behavior."
  },
  "excludeTaskDetails": {
    "type": "boolean",
    "desc": "Excludes all task-related `HistoricDetails`, so only items which have no task id set will be selected.
             When this parameter is used together with `taskId`, this call is ignored and task details are not excluded.
             Value may only be `true`, as `false` is the default behavior."
  },
  "initial": {
    "type": "boolean",
    "desc": "Restrict to historic variable updates that contain only initial variable values.
             Value may only be `true`, as `false` is the default behavior."
  },
  "occurredBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to historic details that occured before the given date (including the date).
             ${dateFormatDescription}"
  },
  "occurredAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to historic details that occured after the given date (including the date).
             ${dateFormatDescription}"
  }
}
/>