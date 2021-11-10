<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/get-query/index.html -->

<#assign sortByValues = [
  '"variableName"',
  '"variableType"',
  '"activityInstanceId"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign variableValuesDesc = "Only include variable instances that have the certain values.
            Value filtering expressions are comma-separated and are structured as
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
  <#assign itemSeperator = "and comma-separated">
<#elseif requestMethod == "POST">
  <#assign variableValuesDesc = "An array to only include variable instances that have the certain values.
           The array consists of objects with the three properties `name`, `operator` and `value`. `name (String)` is the
           variable name, `operator (String)` is the comparison operator to be used and `value` the variable value.
           `value` may be `String`, `Number` or `Boolean`.

           Valid operator values are: `eq` - equal to; `neq` - not equal to; `gt` - greater than; `gteq` - greater
           than or equal to; `lt` - lower than; `lteq` - lower than or equal to; `like`">
  <#assign itemSeperator = "">
</#if>

<#assign params = {
  "variableName": {
    "type": "string",
    "desc": "Filter by variable instance name."
  },
  "variableNameLike": {
    "type": "string",
    "desc": "Filter by the variable instance name. The parameter can include the wildcard `%` to
            express like-strategy such as: starts with (`%`name), ends with (name`%`) or
            contains (`%`name`%`)."
  },
  "processInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator}
            process instance ids."
  },
  "executionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator}
            execution ids."
  },
  "caseInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator} case instance ids."
  },
  "caseExecutionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator} case execution ids."
  },
  "taskIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator} task
            ids."
  },
  "batchIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator}
            batch ids."
  },
  "activityInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator}
            activity instance ids."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of the passed ${itemSeperator}
            tenant ids."
  },
  "variableValues": {
    "type": "array",
    "dto": "VariableQueryParameterDto",
    "desc": "${variableValuesDesc}"
  },
  "variableNamesIgnoreCase": {
    "type": "boolean",
    "desc": "Match all variable names provided in `variableValues` case-insensitively. If set to `true`
            **variableName** and **variablename** are treated as equal."
  },
  "variableValuesIgnoreCase": {
    "type": "boolean",
    "desc": "Match all variable values provided in `variableValues` case-insensitively. If set to
            `true` **variableValue** and **variablevalue** are treated as equal."
  },
  "variableScopeIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include variable instances which belong to one of passed scope ids."
  }
}>
