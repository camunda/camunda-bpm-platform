{
  "type": "object",
  "description": "A historic process instance query which defines a group of historic process instances",
  "properties": {
    "processInstanceId": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by process instance id."
    },
    "processInstanceIds": {
      "uniqueItems": true,
      "type": "array",
      "writeOnly": true,
      "description": "Filter by process instance ids. Must be a JSON array process instance ids.",
      "items": {
        "type": "string"
      }
    },
    "processDefinitionId": {
      "type": "string",
      "description": "Filter by the process definition the instances run on."
    },
    "processDefinitionKey": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by the key of the process definition the instances run on."
    },
    "processDefinitionKeyIn": {
      "type": "array",
      "writeOnly": true,
      "items": {
        "type": "string"
      },
      "description": "Filter by a list of process definition keys. A process instance must have one of the given process definition keys. Must be a JSON array of Strings."
    },
    "processDefinitionName": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by the name of the process definition the instances run on."
    },
    "processDefinitionNameLike": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by process definition names that the parameter is a substring of."
    },
    "processDefinitionKeyNotIn": {
      "type": "array",
      "writeOnly": true,
      "description": "Exclude instances that belong to a set of process definitions. Must be a JSON array of process definition keys.",
      "items": {
        "type": "string"
      }
    },
    "processInstanceBusinessKey": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by process instance business key."
    },
    "processInstanceBusinessKeyLike": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by process instance business key that the parameter is a substring of."
    },
    "rootProcessInstances": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Restrict the query to all process instances that are top level process instances."
    },
    "finished": {
      "type": "boolean",
      "description": "Only include finished process instances. Value may only be true, as false is the default behavior.",
      "writeOnly": true
    },
    "unfinished": {
      "type": "boolean",
      "description": "Only include unfinished process instances. Value may only be true, as false is the default behavior."
    },
    "withIncidents": {
      "type": "boolean",
      "description": "Only include process instances which have an incident. Value may only be true, as false is the default behavior."
    },
    "withRootIncidents": {
      "type": "boolean",
      "description": "Only include process instances which have a root incident. Value may only be true, as false is the default behavior."
    },
    "incidentType": {
      "type": "string",
      "description": "Filter by the incident type. See the User Guide for a list of incident types. (https://docs.camunda.org/manual/develop/user-guide/process-engine/incidents/#incident-types)"
    },
    "incidentStatus": {
      "type": "string",
      "description": "Only include process instances which have an incident in status either open or resolved. To get all process instances, use the query parameter withIncidents.",
      "enum": [
        "open",
        "resolved"
      ]
    },
    "incidentMessage": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by the incident message. Exact match."
    },
    "incidentMessageLike": {
      "type": "string",
      "writeOnly": true,
      "description": "Filter by the incident message that the parameter is a substring of."
    },
    "startedBefore": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "\tRestrict to instances that were started before the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "startedAfter": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that were started after the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "finishedBefore": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that were finished before the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "finishedAfter": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that were finished after the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "executedActivityAfter": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that executed an activity after the given date (inclusive). By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "executedActivityBefore": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that executed an activity before the given date (inclusive). By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "executedJobAfter": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that executed an job after the given date (inclusive). By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "executedJobBefore": {
      "type": "string",
      "format": "date-time",
      "writeOnly": true,
      "description": "Restrict to instances that executed an job before the given date (inclusive). By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200."
    },
    "startedBy": {
      "type": "string",
      "writeOnly": true,
      "description": "Only include process instances that were started by the given user."
    },
    "superProcessInstanceId": {
      "type": "string",
      "writeOnly": true,
      "description": "Restrict query to all process instances that are sub process instances of the given process instance. Takes a process instance id."
    },
    "subProcessInstanceId": {
      "type": "string",
      "writeOnly": true,
      "description": "Restrict query to one process instance that has a sub process instance with the given id."
    },
    "superCaseInstanceId": {
      "type": "string",
      "writeOnly": true,
      "description": "Restrict query to all process instances that are sub process instances of the given case instance. Takes a case instance id."
    },
    "subCaseInstanceId": {
      "type": "string",
      "writeOnly": true,
      "description": "Restrict query to one process instance that has a sub case instance with the given id."
    },
    "caseInstanceId": {
      "type": "string",
      "writeOnly": true,
      "description": "Restrict query to all process instances that are sub process instances of the given case instance. Takes a case instance id."
    },
    "tenantIdIn": {
      "type": "array",
      "writeOnly": true,
      "description": "Filter by a list of tenant ids. A process instance must have one of the given tenant ids. Must be a JSON array of Strings.",
      "items": {
        "type": "string"
      }
    },
    "withoutTenantId": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Only include historic process instances which belong to no tenant. Value may only be true, as false is the default behavior."
    },
    "executedActivityIdIn": {
      "type": "array",
      "writeOnly": true,
      "description": "Restrict to instances that executed an activity with one of given ids.",
      "items": {
        "type": "string"
      }
    },
    "activeActivityIdIn": {
      "type": "array",
      "writeOnly": true,
      "description": "Restrict to instances that have an active activity with one of given ids.",
      "items": {
        "type": "string"
      }
    },
    "active": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Restrict to instances that are active"
    },
    "suspended": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Restrict to instances that are suspended"
    },
    "completed": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Restrict to instances that are completed"
    },
    "externallyTerminated": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Restrict to instances that are externally terminated"
    },
    "internallyTerminated": {
      "type": "boolean",
      "writeOnly": true,
      "description": "\tRestrict to instances that are internally terminated"
    },
    "variables": {
      "type": "array",
      "writeOnly": true,
      "description": "A JSON array to only include process instances that have/had variables with certain values.\nThe array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.\nvalue may be String, Number or Boolean.\nValid operator values are: eq - equal to; neq - not equal to; gt - greater than; gteq - greater than or equal to; lt - lower than; lteq - lower than or equal to; like.",
      "items": {
        "$ref": "#/components/schemas/VariableQueryParameterDto"
      }
    },
    "variableNamesIgnoreCase": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Match all variable names provided in variables case-insensitively. If set to true variableName and variablename are treated as equal."
    },
    "variableValuesIgnoreCase": {
      "type": "boolean",
      "writeOnly": true,
      "description": "Match all variable values provided in variables case-insensitively. If set to true variableValue and variablevalue are treated as equal."
    },
    "orQueries": {
      "type": "array",
      "description": "A JSON array of nested historic process instance queries with OR semantics. A process instance matches a nested query if it fulfills at least one of the query's predicates. With multiple nested queries, a process instance must fulfill at least one predicate of each query (Conjunctive Normal Form).\n\nAll process instance query properties can be used except for: sorting\n\nSee the user guide for more information about OR queries.",
      "items": {
        "$ref": "#/components/schemas/HistoricProcessInstanceQueryDto"
      }
    },
    "sorting": {
      "type": "array",
      "items": {
        "$ref": "#/components/schemas/SortingDto"
      }
    }
  }
}