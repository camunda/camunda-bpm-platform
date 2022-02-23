<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/get-variable-instance-query/index.html -->
<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/post-variable-instance-query/index.html -->

<#assign sortByValues = [
  '"instanceId"','"variableName"','"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign variableValueDescription = "Is treated as a `String` object on server side.">
  <#assign paramListType = "and comma-separated">
<#elseif requestMethod == "POST">
  <#assign variableValueDescription = "May be `String`, `Number` or `Boolean`.">
  <#assign paramListType = "">
</#if>
            
<#assign params = {
  "variableName": {
    "type": "string",
    "desc": "Filter by variable name."
  },
  "variableNameLike": {
    "type": "string",
    "desc": "Restrict to variables with a name like the parameter."
  },
  "variableValue": {
    "type": "object",
    "desc": "Filter by variable value. ${variableValueDescription}"
  },
  "variableNamesIgnoreCase": {
    "type": "boolean",
    "desc": "Match the variable name provided in `variableName` and `variableNameLike` case-
             insensitively. If set to `true` **variableName** and **variablename** are
             treated as equal."
  },
  "variableValuesIgnoreCase": {
    "type": "boolean",
    "desc": "Match the variable value provided in `variableValue` case-insensitively. If set to `true`
             **variableValue** and **variablevalue** are treated as equal."
  },
  "variableTypeIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and comma-
             separated variable types. A list of all supported variable types can be found
             [here](${docsUrl}/user-guide/process-engine/variables/#supported-variable-values).
             **Note:** All non-primitive variables are associated with the type
             'serializable'."
  },
  "includeDeleted": {
    "type": "boolean",
    "desc": "Include variables that has already been deleted during the execution."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by the process instance the variable belongs to."
  },
  "processInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed ${paramListType} process instance ids."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by the process definition the variable belongs to."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by a key of the process definition the variable belongs to."
  },
  "executionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and ${paramListType} execution ids."
  },
  "caseInstanceId": {
    "type": "string",
    "desc": "Filter by the case instance the variable belongs to."
  },
  "caseExecutionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and ${paramListType} case execution ids."
  },
  "caseActivityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and ${paramListType} case activity ids."
  },
  "taskIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and ${paramListType} task ids."
  },
  "activityInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and ${paramListType} activity instance ids."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed and comma-
             separated tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic variable instances that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  },
  "variableNameIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include historic variable instances which belong to one of the passed ${paramListType} variable names."
  }
}>
