  <@lib.parameter
      name = "externalTaskId"
      location = "query"
      type = "string"
      desc = "Filter by an external task's id." />

  <@lib.parameter
      name = "externalTaskIdIn"
      location = "query"
      type = "string"
      desc = "Filter by the comma-separated list of external task ids." />

  <@lib.parameter
      name = "topicName"
      location = "query"
      type = "string"
      desc = "Filter by an external task topic." />

  <@lib.parameter
      name = "workerId"
      location = "query"
      type = "string"
      desc = "Filter by the id of the worker that the task was most recently locked by." />

  <@lib.parameter
      name = "locked"
      location = "query"
      type = "boolean"
      desc = "Only include external tasks that are currently locked (i.e., they have a lock time and it has not expired).
              Value may only be `true`, as `false` matches any external task." />

  <@lib.parameter
      name = "notLocked"
      location = "query"
      type = "boolean"
      desc = "Only include external tasks that are currently not locked (i.e., they have no lock or it has expired).
              Value may only be `true`, as `false` matches any external task." />

  <@lib.parameter
      name = "withRetriesLeft"
      location = "query"
      type = "boolean"
      desc = "Only include external tasks that have a positive (&gt; 0) number of retries (or `null`). Value may only be
              `true`, as `false` matches any external task." />

  <@lib.parameter
      name = "noRetriesLeft"
      location = "query"
      type = "boolean"
      desc = "Only include external tasks that have 0 retries. Value may only be `true`, as `false` matches any
              external task." />

  <@lib.parameter
      name = "lockExpirationAfter"
      location = "query"
      type = "string"
      format = "date-time"
      desc = "Restrict to external tasks that have a lock that expires after a given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
              `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

  <@lib.parameter
      name = "lockExpirationBefore"
      location = "query"
      type = "string"
      format = "date-time"
      desc = "Restrict to external tasks that have a lock that expires before a given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
              `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

  <@lib.parameter
      name = "activityId"
      location = "query"
      type = "string"
      desc = "Filter by the id of the activity that an external task is created for." />

  <@lib.parameter
      name = "activityIdIn"
      location = "query"
      type = "string"
      desc = "Filter by the comma-separated list of ids of the activities that an external task is created for." />

  <@lib.parameter
      name = "executionId"
      location = "query"
      type = "string"
      desc = "Filter by the id of the execution that an external task belongs to." />

  <@lib.parameter
      name = "processInstanceId"
      location = "query"
      type = "string"
      desc = "Filter by the id of the process instance that an external task belongs to." />

  <@lib.parameter
      name = "processInstanceIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of process instance ids that an external task may belong to." />

  <@lib.parameter
      name = "processDefinitionId"
      location = "query"
      type = "string"
      desc = "Filter by the id of the process definition that an external task belongs to." />

  <@lib.parameter
      name = "tenantIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of tenant ids.
              An external task must have one of the given tenant ids." />

  <@lib.parameter
      name = "active"
      location = "query"
      type = "boolean"
      desc = "Only include active tasks. Value may only be `true`, as `false` matches any external task." />

  <@lib.parameter
      name = "suspended"
      location = "query"
      type = "boolean"
      desc = "Only include suspended tasks. Value may only be `true`, as `false` matches any external task." />

  <@lib.parameter
      name = "priorityHigherThanOrEquals"
      location = "query"
      type = "integer"
      format = "int64"
      desc = "Only include jobs with a priority higher than or equal to the given value.
              Value must be a valid `long` value." />

  <@lib.parameter
      name = "priorityLowerThanOrEquals"
      location = "query"
      type = "integer"
      format = "int64"
      desc = "Only include jobs with a priority lower than or equal to the given value.
              Value must be a valid `long` value." />

  <@lib.parameter
      name = "processDefinitionKey"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with the given key." />

  <@lib.parameter
      name = "processDefinitionKeyIn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with one of the given keys. The
              keys need to be in a comma-separated list." />

  <@lib.parameter
      name = "processDefinitionName"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with the given name." />

  <@lib.parameter
      name = "processDefinitionNameLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a process definition name that has the parameter value as
              a substring." />

  <@lib.parameter
      name = "variableNamesIgnoreCase"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Match all variable names in this query case-insensitively. If set
              `variableName` and `variablename` are treated as equal." />

  <@lib.parameter
      name = "variableValuesIgnoreCase"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Match all variable values in this query case-insensitively. If set
              `variableValue` and `variablevalue` are treated as equal." />

  <@lib.parameter
      name = "processVariables"
      location = "query"
      type = "string"
      last = last
      desc = "Only include tasks that belong to process instances that have variables with certain
              values. Variable filtering expressions are comma-separated and are structured as
              follows:

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
              `like`;
              `notLike`.
              `key` and `value` may not contain underscore or comma characters." />