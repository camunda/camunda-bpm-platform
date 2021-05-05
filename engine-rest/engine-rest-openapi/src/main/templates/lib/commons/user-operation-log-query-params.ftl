<#assign sortByValues = [
  '"timestamp"'
]>
<#assign dateFormatDescription = "By [default](${docsUrl}/reference/rest/overview/date-format/), the
                                  timestamp must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`,
                                  e.g., 2013-01-23T14:42:45.000+0200."/>
<#if requestMethod == "GET">
  <#assign listType = "comma-separated ">
<#elseif requestMethod == "POST">
  <#assign listType = "">
</#if>
            
<#assign params = {
  "deploymentId": {
    "type": "string",
    "desc": "Filter by deployment id."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by process definition id."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by process definition key."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by process instance id."
  },
  "executionId": {
    "type": "string",
    "desc": "Filter by execution id."
  },
  "caseDefinitionId": {
    "type": "string",
    "desc": "Filter by case definition id."
  },
  "caseInstanceId": {
    "type": "string",
    "desc": "Filter by case instance id."
  },
  "caseExecutionId": {
    "type": "string",
    "desc": "Filter by case execution id."
  },
  "taskId": {
    "type": "string",
    "desc": "Only include operations on this task."
  },
  "externalTaskId": {
    "type": "string",
    "desc": "Only include operations on this external task."
  },
  "batchId": {
    "type": "string",
    "desc": "Only include operations on this batch."
  },
  "jobId": {
    "type": "string",
    "desc": "Filter by job id."
  },
  "jobDefinitionId": {
    "type": "string",
    "desc": "Filter by job definition id."
  },
  "userId": {
    "type": "string",
    "desc": "Only include operations of this user."
  },
  "operationId": {
    "type": "string",
    "desc": "Filter by the id of the operation. This allows fetching of multiple entries which are part
             of a composite operation."
  },
  "operationType": {
    "type": "string",
    "desc": "Filter by the type of the operation like `Claim` or `Delegate`. See the
             [Javadoc](${docsUrl}/reference/javadoc/?org/camunda/bpm/engine/history/UserOperationLogEntry.html)
             for a list of available operation types."
  },
  "entityType": {
    "type": "string",
    "desc": "Filter by the type of the entity that was affected by this operation, possible values are
             `Task`, `Attachment` or `IdentityLink`."
  },
  "entityTypeIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listType}list of types of the entities that was affected by this operation,
             possible values are `Task`, `Attachment` or `IdentityLink`."
  },
  "category": {
    "type": "string",
    "desc": "Filter by the category that this operation is associated with, possible values are
             `TaskWorker`, `Admin` or `Operator`."
  },
  "categoryIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listType}list of categories that this operation is associated with, possible values are
             `TaskWorker`, `Admin` or `Operator`."
  },
  "property": {
    "type": "string",
    "desc": "Only include operations that changed this property, e.g., `owner` or `assignee`."
  },
  "afterTimestamp": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to entries that were created after the given timestamp. ${dateFormatDescription}"
  },
  "beforeTimestamp": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to entries that were created before the given timestamp. ${dateFormatDescription}"
  }
}>
