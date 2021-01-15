<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/get-query/index.html -->

<#assign sortByValues = [
  '"instanceId"',
  '"definitionKey"',
  '"definitionId"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign listType = "comma-separated">
  <#assign variablesDesc = "Only include executions that have variables with certain values.
             Variable filtering expressions are comma-separated and are structured as
             follows:

             A valid parameter value has the form `key_operator_value`.
             `key` is the variable name, `operator` is the comparison operator to be used
             and `value` the variable value.
             **Note:** Values are always treated as `String` objects on server side.

             Valid operator values are: `eq` - equal to; `neq` - not equal to; `gt` -
             greater than;
             `gteq` - greater than or equal to; `lt` - lower than; `lteq` - lower than or
             equal to;
             `like`.
             `key` and `value` may not contain underscore or comma characters.">

  <#assign processVariablesDesc = "Only include executions that belong to a process instance with variables with certain
             values.
             Variable filtering expressions are comma-separated and are structured as
             follows:

             A valid parameter value has the form `key_operator_value`.
             `key` is the variable name, `operator` is the comparison operator to be used
             and `value` the variable value.
             **Note:** Values are always treated as `String` objects on server side.

             Valid operator values are: `eq` - equal to; `neq` - not equal to.
             `key` and `value` may not contain underscore or comma characters.">

<#elseif requestMethod == "POST">
  <#assign listType = "">
  <#assign variablesDesc = "An array to only include executions that have variables with certain values.

             The array consists of objects with the three properties `name`, `operator`
             and `value`.
             `name (String)` is the variable name, `operator (String)` is the comparison
             operator to be used and `value` the variable value.
             `value` may be `String`, `Number` or `Boolean`.

             Valid operator values are: `eq` - equal to; `neq` - not equal to; `gt` -
             greater than;
             `gteq` - greater than or equal to; `lt` - lower than; `lteq` - lower than or
             equal to;
             `like`.">
  <#assign processVariablesDesc = "An array to only include executions that belong to a process instance with variables
             with certain values.

             The array consists of objects with the three properties `name`, `operator`
             and `value`.
             `name (String)` is the variable name, `operator (String)` is the comparison
             operator to be used and `value` the variable value.
             `value` may be `String`, `Number` or `Boolean`.

             Valid operator values are: `eq` - equal to; `neq` - not equal to.">
</#if>
            
<#assign params = {
  "businessKey": {
    "type": "string",
    "desc": "Filter by the business key of the process instances the executions belong to."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by the process definition the executions run on."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the process definition the executions run on."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by the id of the process instance the execution belongs to."
  },
  "activityId": {
    "type": "string",
    "desc": "Filter by the id of the activity the execution currently executes."
  },
  "signalEventSubscriptionName": {
    "type": "string",
    "desc": "Select only those executions that expect a signal of the given name."
  },
  "messageEventSubscriptionName": {
    "type": "string",
    "desc": "Select only those executions that expect a message of the given name."
  },
  "active": {
    "type": "boolean",
    "desc": "Only include active executions. Value may only be `true`, as `false` is the default
             behavior."
  },
  "suspended": {
    "type": "boolean",
    "desc": "Only include suspended executions. Value may only be `true`, as `false` is the default
             behavior."
  },
  "incidentId": {
    "type": "string",
    "desc": "Filter by the incident id."
  },
  "incidentType": {
    "type": "string",
    "desc": "Filter by the incident type. See the [User Guide](/manual/develop/user-guide/process-engine/incidents/#incident-types) for a list of incident types."
  },
  "incidentMessage": {
    "type": "string",
    "desc": "Filter by the incident message. Exact match."
  },
  "incidentMessageLike": {
    "type": "string",
    "desc": "Filter by the incident message that the parameter is a substring of."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listType} list of tenant ids. An execution must have one of the given
             tenant ids."
  },
  "variables": {
    "type": "array",
    "dto": "VariableQueryParameterDto",
    "desc": "${variablesDesc}"
  },
  "processVariables": {
    "type": "array",
    "dto": "VariableQueryParameterDto",
    "desc": "${processVariablesDesc}"
  },
  "variableNamesIgnoreCase": {
    "type": "boolean",
    "desc": "Match all variable names provided in `variables` and `processVariables` case-
             insensitively. If set to `true` **variableName** and **variablename** are
             treated as equal."
  },
  "variableValuesIgnoreCase": {
    "type": "boolean",
    "desc": "Match all variable values provided in `variables` and `processVariables` case-
             insensitively. If set to `true` **variableValue** and **variablevalue** are
             treated as equal."
  }
}>
