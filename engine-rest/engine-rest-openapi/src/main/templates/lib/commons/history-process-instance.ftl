<#assign sortByValues = [
  '"instanceId"',
  '"definitionId"',
  '"definitionKey"',
  '"definitionName"',
  '"definitionVersion"',
  '"businessKey"',
  '"startTime"',
  '"endTime"',
  '"duration"',
  '"tenantId"'
]>

<#if requestMethod == "GET">

  <#assign listTypeDescription = "Filter by a comma-separated list of `Strings`" />

  <#assign variableDescription>Only include process instances that have/had variables with certain values.
      Variable filtering expressions are comma-separated and are structured as follows:
      A valid parameter value has the form `key_operator_value`. `key` is the variable name, `operator` is the comparison operator to be used and `value` the variable value.

      **Note:** Values are always treated as String objects on server side.

      Valid operator values are: `eq` - equal to; `neq` - not equal to; `gt` - greater than; `gteq` - greater than or equal to; `lt` - lower than; `lteq` - lower than or equal to; `like`.

      Key and value may not contain underscore or comma characters.
  </#assign>

<#elseif requestMethod == "POST">

  <#assign listTypeDescription = "Must be a JSON array of `Strings`">

  <#assign variableDescription>A JSON array to only include process instances that have/had variables with certain values.
      The array consists of objects with the three properties `name`, `operator` and `value`. `name` (`String`) is the variable name,
      `operator` (`String`) is the comparison operator to be used and `value` the variable value.

      Value may be `String`, `Number` or `Boolean`.

      Valid operator values are: `eq` - equal to; `neq` - not equal to; `gt` - greater than; `gteq` - greater than or equal to; `lt` - lower than; `lteq` - lower than or equal to; `like`.
  </#assign>
</#if>

<#assign params = {
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by process instance id."
  },

  "processInstanceIds": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by process instance ids. ${listTypeDescription}."
  },

  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by the process definition the instances run on."
  },

  "processDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the process definition the instances run on."
  },

  "processDefinitionKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a list of process definition keys. A process instance must have one of the given process definition keys. ${listTypeDescription}."
  },
  "processDefinitionName": {
    "type": "string",
    "desc": "Filter by the name of the process definition the instances run on."
  },

  "processDefinitionNameLike": {
    "type": "string",
    "desc": "Filter by process definition names that the parameter is a substring of."
  },

  "processDefinitionKeyNotIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Exclude instances that belong to a set of process definitions. ${listTypeDescription}."
  },
  "processInstanceBusinessKey": {
    "type": "string",
    "desc": "Filter by process instance business key."
  },
  "processInstanceBusinessKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a list of business keys. A process instance must have one of the given business keys. ${listTypeDescription}"
  },

  "processInstanceBusinessKeyLike": {
    "type": "string",
    "desc": "Filter by process instance business key that the parameter is a substring of."
  },

  "rootProcessInstances": {
    "type": "boolean",
    "desc": "Restrict the query to all process instances that are top level process instances."
  },

  "finished": {
    "type": "boolean",
    "desc": "Only include finished process instances. This flag includes all process instances
             that are completed or terminated. Value may only be `true`, as `false` is the default behavior."
  },

  "unfinished": {
    "type": "boolean",
    "desc": "Only include unfinished process instances. Value may only be `true`, as `false` is the default behavior."
  },

  "withIncidents": {
    "type": "boolean",
    "desc": "Only include process instances which have an incident. Value may only be `true`, as `false` is the default behavior."
  },

  "withRootIncidents": {
    "type": "boolean",
    "desc": "Only include process instances which have a root incident. Value may only be `true`, as `false` is the default behavior."
  },

  "incidentIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restrict to instances that have an incident with one of the given ids. ${listTypeDescription}"
  },

  "incidentType": {
    "type": "string",
    "desc": "Filter by the incident type. See the [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types) for a list of incident types."
  },
  "incidentStatus": {
    "type": "string",
    "enumValues": ["open", "resolved"],
    "desc": "Only include process instances which have an incident in status either open or resolved. To get all process instances, use the query parameter withIncidents."
  },
  "incidentMessage": {
    "type": "string",
    "desc": "Filter by the incident message. Exact match."
  },

  "incidentMessageLike": {
    "type": "string",
    "desc": "Filter by the incident message that the parameter is a substring of."
  },

  "startedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were started before the given date.
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "startedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were started after the given date.
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "finishedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were finished before the given date.
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "finishedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that were finished after the given date.
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "executedActivityAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that executed an activity after the given date (inclusive).
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "executedActivityBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that executed an activity before the given date (inclusive).
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "executedJobAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that executed an job after the given date (inclusive).
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "executedJobBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to instances that executed an job before the given date (inclusive).
             By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },
  "startedBy": {
    "type": "string",
    "desc": "Only include process instances that were started by the given user."
  },

  "superProcessInstanceId": {
    "type": "string",
    "desc": "Restrict query to all process instances that are sub process instances of the given process instance. Takes a process instance id."
  },

  "subProcessInstanceId": {
    "type": "string",
    "desc": "Restrict query to one process instance that has a sub process instance with the given id."
  },

  "superCaseInstanceId": {
    "type": "string",
    "desc": "Restrict query to all process instances that are sub process instances of the given case instance. Takes a case instance id."
  },

  "subCaseInstanceId": {
    "type": "string",
    "desc": "Restrict query to one process instance that has a sub case instance with the given id."
  },

  "caseInstanceId": {
    "type": "string",
    "desc": "Restrict query to all process instances that are sub process instances of the given case instance. Takes a case instance id."
  },

  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a list of tenant ids. A process instance must have one of the given tenant ids. ${listTypeDescription}"
  },

  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic process instances which belong to no tenant. Value may only be `true`, as `false` is the default behavior."
  },

  "activityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restrict to instances with an active activity with one of the given ids. In contrast to the `activeActivityIdIn` filter, it can query for async and incident activities. ${listTypeDescription}"
  },

  "executedActivityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restrict to instances that executed an activity with one of given ids. ${listTypeDescription}"
  },

  "activeActivityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restrict to instances that have an active activity with one of given ids. ${listTypeDescription}"
  },

  "active": {
    "type": "boolean",
    "desc": "Restrict to instances that are active."
  },

  "suspended": {
    "type": "boolean",
    "desc": "Restrict to instances that are suspended."
  },

  "completed": {
    "type": "boolean",
    "desc": "Restrict to instances that are completed."
  },

  "externallyTerminated": {
    "type": "boolean",
    "desc": "Restrict to instances that are externallyTerminated."
  },

  "internallyTerminated": {
    "type": "boolean",
    "desc": "Restrict to instances that are internallyTerminated."
  },

  "variables": {
    "type": "array",
    "dto": "VariableQueryParameterDto",
    "desc": "${variableDescription}"
  },

  "variableNamesIgnoreCase": {
    "type": "boolean",
    "desc": "Match all variable names provided in variables case-insensitively. If set to `true` variableName and variablename are treated as equal."
  },

  "variableValuesIgnoreCase": {
    "type": "boolean",
    "desc": "Match all variable values provided in variables case-insensitively. If set to `true` variableValue and variablevalue are treated as equal."
  },

  "orQueries": {
    "type": "array",
    "dto": "HistoricProcessInstanceQueryDto",
    "desc": "A JSON array of nested historic process instance queries with OR semantics.

             A process instance matches a nested query if it fulfills at least one of the query's predicates.

             With multiple nested queries, a process instance must fulfill at least one predicate of each query
             ([Conjunctive Normal Form](https://en.wikipedia.org/wiki/Conjunctive_normal_form)).

             All process instance query properties can be used except for: `sorting`

             See the [User Guide](${docsUrl}/user-guide/process-engine/process-engine-api/#or-queries) for more information about OR queries."
  }
}>
