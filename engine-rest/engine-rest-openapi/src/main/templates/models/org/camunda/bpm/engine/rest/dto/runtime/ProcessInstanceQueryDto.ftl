{
  "type": "object",
  "description": "A process instance query which defines a group of process instances",
  "properties": {
    <@lib.property
        name="deploymentId"
        type="string"
        description="Filter by the deployment the id belongs to."/>

    <@lib.property
        name="processDefinitionId"
        type="string"
        description="Filter by the process definition the instances run on."/>

    <@lib.property
        name="processDefinitionKey"
        type="string"
        description="Filter by the key of the process definition the instances run on."/>

<#--     "deploymentId": {
      "type": "string",
      "description": "Filter by the deployment the id belongs to."
    },
    "processDefinitionId": {
      "type": "string",
      "description": "Filter by the process definition the instances run on."
    },
    "processDefinitionKey": {
      "type": "string",
      "description": "Filter by the key of the process definition the instances run on."
    }, -->
    "processDefinitionKeyIn": {
      "type": "array",
      "writeOnly": true,
      "description": "Filter by a list of process definition keys. A process instance must have one of the given process definition keys. Must be a JSON array of Strings.",
      "items": {
        "type": "string"
      }
    },
    "processDefinitionKeyNotIn": {
      "type": "array",
      "description": "Exclude instances by a list of process definition keys. A process instance must not have one of the given process definition keys. Must be a JSON array of Strings.",
      "items": {
        "type": "string"
      }
    },
    "businessKey": {
      "type": "string",
      "description": "Filter by process instance business key."
    },
    "businessKeyLike": {
      "type": "string",
      "description": "Filter by process instance business key that the parameter is a substring of."
    },
    "caseInstanceId": {
      "type": "string",
      "description": "Filter by case instance id."
    },
    "superProcessInstance": {
      "type": "string",
      "description": "Restrict query to all process instances that are sub process instances of the given process instance. Takes a process instance id."
    },
    "subProcessInstance": {
      "type": "string",
      "description": "Restrict query to all process instances that have the given process instance as a sub process instance. Takes a process instance id."
    },
    "superCaseInstance": {
      "type": "string",
      "description": "Restrict query to all process instances that are sub process instances of the given case instance. Takes a case instance id."
    },
    "subCaseInstance": {
      "type": "string",
      "description": "Restrict query to all process instances that have the given case instance as a sub case instance. Takes a case instance id."
    },
    "active": {
      "type": "boolean",
      "description": "Only include active process instances. Value may only be true, as false is the default behavior."
    },
    "suspended": {
      "type": "boolean",
      "description": "Only include suspended process instances. Value may only be true, as false is the default behavior."
    },
    "processInstanceIds": {
      "uniqueItems": true,
      "type": "array",
      "description": "Filter by a list of process instance ids. Must be a JSON array of Strings.",
      "items": {
        "type": "string"
      }
    },
    "withIncident": {
      "type": "boolean",
      "description": "Filter by presence of incidents. Selects only process instances that have an incident."
    },
    "incidentId": {
      "type": "string",
      "description": "Filter by the incident id."
    },
    "incidentType": {
      "type": "string",
      "description": "Filter by the incident type. See the User Guide for a list of incident types."
    },
    "incidentMessage": {
      "type": "string",
      "description": "Filter by the incident message. Exact match."
    },
    "incidentMessageLike": {
      "type": "string",
      "description": "Filter by the incident message that the parameter is a substring of."
    },
    "tenantIdIn": {
      "type": "array",
      "description": "Filter by a list of tenant ids. A process instance must have one of the given tenant ids. Must be a JSON array of Strings.",
      "items": {
        "type": "string"
      }
    },
    "withoutTenantId": {
      "type": "boolean",
      "description": "Only include process instances which belong to no tenant. Value may only be true, as false is the default behavior."
    },
    "processDefinitionWithoutTenantId": {
      "type": "boolean",
      "description": "Only include process instances which process definition has no tenant id."
    },
    "activityIdIn": {
      "type": "array",
      "writeOnly": true,
      "description": "Filter by a list of activity ids. A process instance must currently wait in a leaf activity with one of the given activity ids.",
      "items": {
        "type": "string"
      }
    },
    "rootProcessInstances": {
      "type": "boolean",
      "description": "Restrict the query to all process instances that are top level process instances."
    },
    "leafProcessInstances": {
      "type": "boolean",
      "description": "Restrict the query to all process instances that are leaf instances. (i.e. don't have any sub instances)"
    },
    "variables": {
      "type": "array",
      "description": "A JSON array to only include process instances that have variables with certain values.\nThe array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.\nvalue may be String, Number or Boolean.\nValid operator values are: eq - equal to; neq - not equal to; gt - greater than; gteq - greater than or equal to; lt - lower than; lteq - lower than or equal to; like.",
      "items": {
        "$ref": "#/components/schemas/VariableQueryParameterDto"
      }
    },
    "variableNamesIgnoreCase": {
      "type": "boolean",
      "description": "Match all variable names in this query case-insensitively. If set to true variableName and variablename are treated as equal."
    },
    "variableValuesIgnoreCase": {
      "type": "boolean",
      "description": "Match all variable values in this query case-insensitively. If set to true variableValue and variablevalue are treated as equal."
    },
    "orQueries": {
      "type": "array",
      "description": "A JSON array of nested process instance queries with OR semantics. A process instance matches a nested query if it fulfills at least one of the query's predicates. With multiple nested queries, a process instance must fulfill at least one predicate of each query (Conjunctive Normal Form).\n\nAll process instance query properties can be used except for: sorting\n\nSee the user guide (https://docs.camunda.org/manual/develop/user-guide/process-engine/process-engine-api/#or-queries) for more information about OR queries.",
      "items": {
        "$ref": "#/components/schemas/ProcessInstanceQueryDto"
      }
    },
    "sorting": {
      "type": "array",
      "description": "A JSON array of criteria to sort the result by. Each element of the array is a JSON object that specifies one ordering. The position in the array identifies the rank of an ordering, i.e., whether it is primary, secondary, etc.",
      "items": {
        "$ref": "#/components/schemas/SortingDto"
      }
    }
  }
}
