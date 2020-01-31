{
  "type": "object",
  "description": "A process instance query which defines a group of process instances",
  "properties": {
    <@lib.property
        name = "deploymentId"
        type = "string"
        description = "Filter by the deployment the id belongs to."/>

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        description = "Filter by the process definition the instances run on."/>

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        description = "Filter by the key of the process definition the instances run on."/>

    <@lib.property
        name = "processDefinitionKeyIn"
        type = "array"
        itemType = "string"
        description = "Filter by a list of process definition keys. A process instance must have one of the given process definition keys. Must be a JSON array of Strings." />

    <@lib.property
        name = "processDefinitionKeyNotIn"
        type = "array"
        itemType = "string"
        description = "Exclude instances by a list of process definition keys. A process instance must not have one of the given process definition keys. Must be a JSON array of Strings." />

    <@lib.property
        name = "businessKey"
        type = "string"
        description = "Filter by process instance business key." />

    <@lib.property
        name = "businessKeyLike"
        type = "string"
        description = "Filter by process instance business key that the parameter is a substring of." />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        description = "Filter by case instance id." />

    <@lib.property
        name = "superProcessInstance"
        type = "string"
        description = "Restrict query to all process instances that are sub process instances of the given process instance. Takes a process instance id." />

    <@lib.property
        name = "subProcessInstance"
        type = "string"
        description = "Restrict query to all process instances that have the given process instance as a sub process instance. Takes a process instance id." />

    <@lib.property
        name = "superCaseInstance"
        type = "string"
        description = "Restrict query to all process instances that are sub process instances of the given case instance. Takes a case instance id." />

    <@lib.property
        name = "subCaseInstance"
        type = "string"
        description = "Restrict query to all process instances that have the given case instance as a sub case instance. Takes a case instance id." />

    <@lib.property
        name = "active"
        type = "boolean"
        description = "Only include active process instances. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "suspended"
        type = "boolean"
        description = "Only include suspended process instances. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        description = "Filter by a list of process instance ids. Must be a JSON array of Strings." />

    <@lib.property
        name = "withIncident"
        type = "boolean"
        description = "Filter by presence of incidents. Selects only process instances that have an incident." />

    <@lib.property
        name = "incidentId"
        type = "string"
        description = "Filter by the incident id." />

    <@lib.property
        name = "incidentType"
        type = "string"
        description = "Filter by the incident type. See the User Guide for a list of incident types." />

    <@lib.property
        name = "incidentMessage"
        type = "string"
        description = "Filter by the incident message. Exact match." />

    <@lib.property
        name = "incidentMessageLike"
        type = "string"
        description = "Filter by the incident message that the parameter is a substring of." />

    <@lib.property
        name = "tenantIdIn"
        type = "array"
        itemType = "string"
        description = "Filter by a list of tenant ids. A process instance must have one of the given tenant ids. Must be a JSON array of Strings." />

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        description = "Only include process instances which belong to no tenant. Value may only be true, as false is the default behavior." />

    <@lib.property
        name = "processDefinitionWithoutTenantId"
        type = "boolean"
        description = "Only include process instances which process definition has no tenant id." />

    <@lib.property
        name = "activityIdIn"
        type = "array"
        itemType = "string"
        description = "Filter by a list of activity ids. A process instance must currently wait in a leaf activity with one of the given activity ids." />

    <@lib.property
        name = "rootProcessInstances"
        type = "boolean"
        description = "Restrict the query to all process instances that are top level process instances." />

    <@lib.property
        name = "leafProcessInstances"
        type = "boolean"
        description = "Restrict the query to all process instances that are leaf instances. (i.e. don't have any sub instances)" />

    <@lib.property
        name = "variables"
        type = "array"
        dto = "VariableQueryParameterDto"
        description = "A JSON array to only include process instances that have variables with certain values.
        The array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.
        The value may be String, Number or Boolean.
        Valid operator values are: eq - equal to; neq - not equal to; gt - greater than; gteq - greater than or equal to; lt - lower than; lteq - lower than or equal to; like." />

    <@lib.property
        name = "variableNamesIgnoreCase"
        type = "boolean"
        description = "Match all variable names in this query case-insensitively. If set to true variableName and variablename are treated as equal." />

    <@lib.property
        name = "variableValuesIgnoreCase"
        type = "boolean"
        description = "Match all variable values in this query case-insensitively. If set to true variableValue and variablevalue are treated as equal." />

    <@lib.property
        name = "orQueries"
        type = "array"
        dto = "ProcessInstanceQueryDto"
        description = "A JSON array of nested process instance queries with OR semantics.
        A process instance matches a nested query if it fulfills at least one of the query's predicates.
        With multiple nested queries, a process instance must fulfill at least one predicate of each query (Conjunctive Normal Form).
        All process instance query properties can be used except for: sorting
        See the User guide (https://docs.camunda.org/manual/${docsVersion}/user-guide/process-engine/process-engine-api/#or-queries) for more information about OR queries." />


    "sorting": {
      "type": "array",
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#assign sortByValues = ['"instanceId"', '"definitionId"', '"definitionKey"', '"businessKey"', '"tenantId"']>
        <#include "/paths/commons/sort-props.ftl" >

    }
  }
}
