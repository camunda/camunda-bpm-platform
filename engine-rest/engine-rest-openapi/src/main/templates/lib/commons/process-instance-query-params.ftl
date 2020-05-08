  <@lib.parameter name = "processInstanceIds"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of process instance ids."/>

  <@lib.parameter name = "businessKey"
      location = "query"
      type = "string"
      desc = "Filter by process instance business key."/>

  <@lib.parameter name = "businessKeyLike"
      location = "query"
      type = "string"
      desc = "Filter by process instance business key that the parameter is a substring of."/>

  <@lib.parameter
      name = "caseInstanceId"
      location = "query"
      type = "string"
      desc = "Filter by case instance id."/>

  <@lib.parameter name = "processDefinitionId"
      location = "query"
      type = "string"
      desc = "Filter by the deployment the id belongs to."/>

  <@lib.parameter name = "processDefinitionKey"
      location = "query"
      type = "string"
      desc = "Filter by the key of the process definition the instances run on."/>

  <@lib.parameter name = "processDefinitionKeyIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of process definition keys.
              A process instance must have one of the given process definition keys."/>

  <@lib.parameter name = "processDefinitionKeyNotIn"
      location = "query"
      type = "string"
      desc = "Exclude instances by a comma-separated list of process definition keys.
              A process instance must not have one of the given process definition keys."/>

  <@lib.parameter name = "deploymentId"
      location = "query"
      type = "string"
      desc = "Filter by the deployment the id belongs to."/>

  <@lib.parameter name = "superProcessInstance"
      location = "query"
      type = "string"
      desc = "Restrict query to all process instances that are sub process instances of the given process instance.
              Takes a process instance id."/>

  <@lib.parameter name = "subProcessInstance"
      location = "query"
      type = "string"
      desc = "Restrict query to all process instances that have the given process instance as a sub process instance.
              Takes a process instance id."/>

  <@lib.parameter name = "superCaseInstance"
      location = "query"
      type = "string"
      desc = "Restrict query to all process instances that are sub process instances of the given case instance.
              Takes a case instance id."/>

  <@lib.parameter name = "subCaseInstance"
      location = "query"
      type = "string"
      desc = "Restrict query to all process instances that have the given case instance as a sub case instance.
              Takes a case instance id."/>

  <@lib.parameter name = "active"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Only include active process instances. Value may only be true,
              as false is the default behavior."/>

  <@lib.parameter name = "suspended"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Only include suspended process instances. Value may only be true,
              as false is the default behavior."/>

  <@lib.parameter name = "withIncident"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Filter by presence of incidents. Selects only process instances that have an incident."/>

  <@lib.parameter name = "incidentId"
      location = "query"
      type = "string"
      desc = "Filter by the incident id."/>

  <@lib.parameter name = "incidentType"
      location = "query"
      type = "string"
      desc = "Filter by the incident type.
              See the [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types)
              for a list of incident types."/>

  <@lib.parameter name = "incidentMessage"
      location = "query"
      type = "string"
      desc = "Filter by the incident message. Exact match."/>

  <@lib.parameter name = "incidentMessageLike"
      location = "query"
      type = "string"
      desc = "Filter by the incident message that the parameter is a substring of."/>

  <@lib.parameter name = "tenantIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of tenant ids. A process instance must have one of the given tenant ids."/>

  <@lib.parameter name = "withoutTenantId"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Only include process instances which belong to no tenant."/>

  <@lib.parameter name = "processDefinitionWithoutTenantId"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Only include process instances which process definition has no tenant id."/>

  <@lib.parameter name = "activityIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of activity ids.
              A process instance must currently wait in a leaf activity with one of the given activity ids."/>

  <@lib.parameter name = "rootProcessInstances"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Restrict the query to all process instances that are top level process instances."/>

  <@lib.parameter name = "leafProcessInstances"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Restrict the query to all process instances that are leaf instances. (i.e. don't have any sub instances)."/>

  <@lib.parameter name = "variables"
      location = "query"
      type = "string"
      desc = "Only include process instances that have variables with certain values.
              Variable filtering expressions are comma-separated and are structured as follows:

              A valid parameter value has the form `key_operator_value`. `key` is the variable name,
              `operator` is the comparison operator to be used and `value` the variable value.

              **Note**: Values are always treated as String objects on server side.

              Valid `operator` values are:
              `eq` - equal to;
              `neq` - not equal to;
              `gt` - greater than;
              `gteq` - greater than or equal to;
              `lt` - lower than;
              `lteq` - lower than or equal to;
              `like`.
              `key` and `value` may not contain underscore or comma characters."/>

  <@lib.parameter name = "variableNamesIgnoreCase"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      desc = "Match all variable names in this query case-insensitively.
              If set to true variableName and variablename are treated as equal."/>

  <@lib.parameter name = "variableValuesIgnoreCase"
      location = "query"
      type = "boolean"
      defaultValue = 'false'
      last = last
      desc = "Match all variable values in this query case-insensitively.
              If set to true variableValue and variablevalue are treated as equal."/>