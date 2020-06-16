<#assign sortByValues = [
  '"activityInstanceId"',
  '"instanceId"',
  '"executionId"',
  '"activityId"',
  '"activityName"',
  '"activityType"',
  '"startTime"',
  '"endTime"',
  '"duration"',
  '"definitionId"',
  '"occurrence"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign listTypeDescription = "Filter by a comma-separated list of ids">
<#elseif requestMethod == "POST">
  <#assign listTypeDescription = "Must be a JSON array of Strings">
</#if>

<#assign params = {
  "activityInstanceId": {
    "type": "string",
    "desc": "Filter by activity instance id."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by process instance id."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by process definition id."
  },
  "executionId": {
    "type": "string",
    "desc": "Filter by the id of the execution that executed the activity instance."
  },
  "activityId": {
    "type": "string",
    "desc": "Filter by the activity id (according to BPMN 2.0 XML)."
  },
  "activityName": {
    "type": "string",
    "desc": "Filter by the activity name (according to BPMN 2.0 XML)."
  },
  "activityType": {
    "type": "string",
    "desc": "Filter by activity type."
  },
  "taskAssignee": {
    "type": "string",
    "desc": "Only include activity instances that are user tasks and assigned to a given user."
  },
  "finished": {
    "type": "boolean",
    "desc": "Only include finished activity instances.
             Value may only be `true`, as `false` behaves the same as when the property is not set."
  },
  "unfinished": {
    "type": "boolean",
    "desc": "Only include unfinished activity instances.
             Value may only be `true`, as `false` behaves the same as when the property is not set."
  },
  "canceled": {
    "type": "boolean",
    "desc": "Only include canceled activity instances.
             Value may only be `true`, as `false` behaves the same as when the property is not set."
  },
  "completeScope": {
    "type": "boolean",
    "desc": "Only include activity instances which completed a scope.
             Value may only be `true`, as `false` behaves the same as when the property is not set."
  },
  "startedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were started before the given date. By [default](${docsUrl}/reference/rest/overview/date-format/),
             the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "startedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were started after the given date. By [default](${docsUrl}/reference/rest/overview/date-format/),
             the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "finishedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were finished before the given date. By [default](${docsUrl}/reference/rest/overview/date-format/),
             the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "finishedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were finished after the given date. By [default](${docsUrl}/reference/rest/overview/date-format/),
             the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "${listTypeDescription}. An activity instance must have one of the given tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic activity instances that belong to no tenant. Value may only be `true`, as `false` is the default behavior."
  }
}>