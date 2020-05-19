<@lib.dto
    desc = "A historic process instance query which defines a group of historic process instances" >

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "Filter by process instance id."/>

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "Filter by process instance ids. Must be a JSON array process instance ids."/>

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "Filter by the process definition the instances run on."/>

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "Filter by the key of the process definition the instances run on."/>

    <@lib.property
        name = "processDefinitionKeyIn"
        type = "array"
        itemType = "string"
        desc = "Filter by a list of process definition keys.
                A process instance must have one of the given process definition keys. Must be a JSON array of Strings."/>

    <@lib.property
        name = "processDefinitionName"
        type = "string"
        desc = "Filter by the name of the process definition the instances run on." />

    <@lib.property
        name = "processDefinitionNameLike"
        type = "string"
        desc = "Filter by process definition names that the parameter is a substring of." />

    <@lib.property
        name = "processDefinitionKeyNotIn"
        type = "array"
        itemType = "string"
        desc = "Exclude instances that belong to a set of process definitions.
                Must be a JSON array of process definition keys."/>

    <@lib.property
        name = "processInstanceBusinessKey"
        type = "string"
        desc = "Filter by process instance business key." />

    <@lib.property
        name = "processInstanceBusinessKeyLike"
        type = "string"
        desc = "Filter by process instance business key that the parameter is a substring of." />

    <@lib.property
        name = "rootProcessInstances"
        type = "boolean"
        desc = "Restrict the query to all process instances that are top level process instances." />

    <@lib.property
        name = "finished"
        type = "boolean"
        defaultValue = 'false'
        desc = "Only include finished process instances. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "unfinished"
        type = "boolean"
        defaultValue = 'false'
        desc = "Only include unfinished process instances. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "withIncidents"
        type = "boolean"
        defaultValue = 'false'
        desc = "Only include process instances which have an incident. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "withRootIncidents"
        type = "boolean"
        defaultValue = '"false"'
        desc = "Only include process instances which have a root incident. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "incidentType"
        type = "string"
        desc = "Filter by the incident type.
                See the [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types) for a list of incident types. " />

    <@lib.property
        name = "incidentStatus"
        type = "string"
        enumValues = ['"open"', '"resolved"']
        desc = "Only include process instances which have an incident in status either open or resolved.
                To get all process instances, use the query parameter withIncidents." />

    <@lib.property
        name = "incidentMessage"
        type = "string"
        desc = "Filter by the incident message. Exact match." />

    <@lib.property
        name = "incidentMessageLike"
        type = "string"
        desc = "Filter by the incident message that the parameter is a substring of." />

    <@lib.property
        name = "startedBefore"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that were started before the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "startedAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that were started after the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "finishedBefore"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that were finished before the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "finishedAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that were finished after the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "executedActivityAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that executed an activity after the given date (inclusive).
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "executedActivityBefore"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that executed an activity before the given date (inclusive).
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "executedJobAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that executed an job after the given date (inclusive).
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "executedJobBefore"
        type = "string"
        format = "date-time"
        desc = "Restrict to instances that executed an job before the given date (inclusive).
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200." />

    <@lib.property
        name = "startedBy"
        type = "string"
        desc = "Only include process instances that were started by the given user." />

    <@lib.property
        name = "superProcessInstanceId"
        type = "string"
        desc = "Restrict query to all process instances that are sub process instances of the given process instance.
                Takes a process instance id." />

    <@lib.property
        name = "subProcessInstanceId"
        type = "string"
        desc = "Restrict query to one process instance that has a sub process instance with the given id." />

    <@lib.property
        name = "superCaseInstanceId"
        type = "string"
        desc = "Restrict query to all process instances that are sub process instances of the given case instance.
                Takes a case instance id." />

    <@lib.property
        name = "subCaseInstanceId"
        type = "string"
        desc = "Restrict query to one process instance that has a sub case instance with the given id." />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "Restrict query to all process instances that are sub process instances of the given case instance.
                Takes a case instance id." />

    <@lib.property
        name = "tenantIdIn"
        type = "array"
        itemType = "string"
        desc = "Filter by a list of tenant ids. A process instance must have one of the given tenant ids.
                Must be a JSON array of Strings." />

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        desc = "Only include historic process instances which belong to no tenant.
                Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "executedActivityIdIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to instances that executed an activity with one of given ids." />

    <@lib.property
        name = "activeActivityIdIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to instances that have an active activity with one of given ids." />

    <@lib.property
        name = "active"
        type = "boolean"
        desc = "Restrict to instances that are active." />

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "Restrict to instances that are suspended." />

    <@lib.property
        name = "completed"
        type = "boolean"
        desc = "Restrict to instances that are completed." />

    <@lib.property
        name = "externallyTerminated"
        type = "boolean"
        desc = "Restrict to instances that are externallyTerminated." />

    <@lib.property
        name = "internallyTerminated"
        type = "boolean"
        desc = "Restrict to instances that are internallyTerminated." />

    <@lib.property
        name = "variables"
        type = "array"
        dto = "VariableQueryParameterDto"
        desc = "A JSON array to only include process instances that have/had variables with certain values.
                The array consists of objects with the three properties `name`, `operator` and `value`.
                `name` (String) is the variable name,
                `operator` (String) is the comparison operator to be used and
                `value` the variable value.
                `value` may be String, Number or Boolean.
                Valid operator values are: `eq` - equal to; `neq` - not equal to; `gt` - greater than; `gteq` - greater than or equal to;
                `lt` - lower than; `lteq` - lower than or equal to; `like`." />

    <@lib.property
        name = "variableNamesIgnoreCase"
        type = "boolean"
        desc = "Match all variable names provided in variables case-insensitively.
                If set to true variableName and variablename are treated as equal." />

    <@lib.property
        name = "variableValuesIgnoreCase"
        type = "boolean"
        desc = "Match all variable values provided in variables case-insensitively.
                If set to true variableValue and variablevalue are treated as equal." />

    <@lib.property
        name = "orQueries"
        type = "array"
        dto = "HistoricProcessInstanceQueryDto"
        desc = "A JSON array of nested historic process instance queries with OR semantics.
                A process instance matches a nested query if it fulfills at least one of the query's predicates.
                With multiple nested queries, a process instance must fulfill at least one predicate of each query (Conjunctive Normal Form).
                All process instance query properties can be used except for: sorting
                See the [User Guide](${docsUrl}/user-guide/process-engine/process-engine-api/#or-queries) for more information about OR queries." />

    "sorting": {
      "type": "array",
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#assign sortByValues = ['"instanceId"', '"definitionId"', '"definitionKey"', '"definitionName"',
                                 '"definitionVersion"', '"businessKey"', '"startTime"', '"endTime"', '"duration"', '"tenantId"']>
        <#include "/lib/commons/sort-props.ftl" >

    }

</@lib.dto>