  <@lib.parameter name = "taskId"
      location = "query"
      type = "string"
      desc = "Restrict to task with the given id." />

  <@lib.parameter name = "taskIdIn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks with any of the given ids." />

  <@lib.parameter name = "processInstanceId"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to process instances with the given id." />

  <@lib.parameter name = "processInstanceIdIn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to process instances with the given ids." />

  <@lib.parameter name = "processInstanceBusinessKey"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to process instances with the given business key." />

  <@lib.parameter name = "processInstanceBusinessKeyExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to process instances with the given business key which 
              is described by an expression. See the 
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions." />

  <@lib.parameter name = "processInstanceBusinessKeyIn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to process instances with one of the give business keys. 
              The keys need to be in a comma-separated list." />

  <@lib.parameter name = "processInstanceBusinessKeyLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a process instance business key that has the parameter 
              value as a substring." />

  <@lib.parameter name = "processInstanceBusinessKeyLikeExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a process instance business key that has the parameter 
              value as a substring and is described by an expression. See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
              for more information on available functions." />

  <@lib.parameter name = "processDefinitionId"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with the given id." />

  <@lib.parameter name = "processDefinitionKey"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with the given key." />

  <@lib.parameter name = "processDefinitionKeyIn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with one of the given keys. The 
              keys need to be in a comma-separated list." />

  <@lib.parameter name = "processDefinitionName"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a process definition with the given name." />

  <@lib.parameter name = "processDefinitionNameLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a process definition name that has the parameter value as 
              a substring." />

  <@lib.parameter name = "executionId"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to an execution with the given id." />

  <@lib.parameter name = "caseInstanceId"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to case instances with the given id." />

  <@lib.parameter name = "caseInstanceBusinessKey"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to case instances with the given business key." />

  <@lib.parameter name = "caseInstanceBusinessKeyLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a case instance business key that has the parameter value 
              as a substring." />

  <@lib.parameter name = "caseDefinitionId"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a case definition with the given id." />

  <@lib.parameter name = "caseDefinitionKey"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a case definition with the given key." />

  <@lib.parameter name = "caseDefinitionName"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a case definition with the given name." />

  <@lib.parameter name = "caseDefinitionNameLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a case definition name that has the parameter value as a 
              substring." />

  <@lib.parameter name = "caseExecutionId"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that belong to a case execution with the given id." />

  <@lib.parameter name = "activityInstanceIdIn"
      location = "query"
      type = "string"
      desc = "Only include tasks which belong to one of the passed and comma-separated activity 
              instance ids." />

  <@lib.parameter name = "tenantIdIn"
      location = "query"
      type = "string"
      desc = "Only include tasks which belong to one of the passed and comma-separated 
              tenant ids." />

  <@lib.parameter name = "withoutTenantId"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include tasks which belong to no tenant. Value may only be `true`, 
              as `false` is the default behavior." />

  <@lib.parameter name = "assignee"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that the given user is assigned to." />

  <@lib.parameter name = "assigneeExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that the user described by the given expression is assigned to. 
              See the 
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
              for more information on available functions." />

  <@lib.parameter name = "assigneeLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have an assignee that has the parameter 
              value as a substring." />

  <@lib.parameter name = "assigneeLikeExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have an assignee that has the parameter value described by the 
              given expression as a substring. See the 
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
              for more information on available functions." />

  <@lib.parameter name = "assigneeIn"
      location = "query"
      type = "string"
      desc = "Only include tasks which are assigned to one of the passed and 
              comma-separated user ids." />

  <@lib.parameter name = "assigneeNotIn"
      location = "query"
      type = "string"
      desc = "Only include tasks which are not assigned to one of the passed and
              comma-separated user ids." />

  <@lib.parameter name = "owner"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that the given user owns." />

  <@lib.parameter name = "ownerExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that the user described by the given expression owns. See the 
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
              for more information on available functions." />

  <@lib.parameter name = "candidateGroup"
      location = "query"
      type = "string"
      desc = "Only include tasks that are offered to the given group." />

  <@lib.parameter name = "candidateGroupLike"
  location = "query"
  type = "string"
  desc = "Only include tasks that are offered to groups that have the parameter value as a substring." />

  <@lib.parameter name = "candidateGroupExpression"
      location = "query"
      type = "string"
      desc = "Only include tasks that are offered to the group described by the given expression. 
              See the 
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
              for more information on available functions." />

  <@lib.parameter name = "candidateUser"
      location = "query"
      type = "string"
      desc = "Only include tasks that are offered to the given user or to one of his groups." />

  <@lib.parameter name = "candidateUserExpression"
      location = "query"
      type = "string"
      desc = "Only include tasks that are offered to the user described by the given expression. 
              See the 
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
              for more information on available functions." />

  <@lib.parameter name = "includeAssignedTasks"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Also include tasks that are assigned to users in candidate queries. Default is to only 
              include tasks that are not assigned to any user if you query by candidate user or
              group(s)." />

  <@lib.parameter name = "involvedUser"
      location = "query"
      type = "string"
      desc = "Only include tasks that the given user is involved in. A user is involved in a task if 
              an identity link exists between task and user (e.g., the user is the assignee)." />

  <@lib.parameter name = "involvedUserExpression"
      location = "query"
      type = "string"
      desc = "Only include tasks that the user described by the given expression is involved in.
              A user is involved in a task if an identity link exists between task and user
              (e.g., the user is the assignee). See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions." />

  <@lib.parameter name = "assigned"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "If set to `true`, restricts the query to all tasks that are assigned." />

  <@lib.parameter name = "unassigned"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "If set to `true`, restricts the query to all tasks that are unassigned." />

  <@lib.parameter name = "taskDefinitionKey"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have the given key." />

  <@lib.parameter name = "taskDefinitionKeyIn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have one of the given keys. The keys need to be in a
              comma-separated list." />

  <@lib.parameter name = "taskDefinitionKeyLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a key that has the parameter value as a substring." />

  <@lib.parameter name = "name"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have the given name." />

  <@lib.parameter name = "nameNotEqual"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that do not have the given name." />

  <@lib.parameter name = "nameLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a name with the given parameter value as substring." />

  <@lib.parameter name = "nameNotLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that do not have a name with the given parameter
              value as substring." />

  <@lib.parameter name = "description"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have the given description." />

  <@lib.parameter name = "descriptionLike"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a description that has the parameter
              value as a substring." />

  <@lib.parameter name = "priority"
      location = "query"
      type = "integer"
      desc = "Restrict to tasks that have the given priority." />

  <@lib.parameter name = "maxPriority"
      location = "query"
      type = "integer"
      desc = "Restrict to tasks that have a lower or equal priority." />

  <@lib.parameter name = "minPriority"
      location = "query"
      type = "integer"
      desc = "Restrict to tasks that have a higher or equal priority." />

  <@lib.parameter name = "dueDate"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are due on the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the
              format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.546+0200`." />

  <@lib.parameter name = "dueDateExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are due on the date described by the given expression. See the
              [User Guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "dueAfter"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are due after the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
              the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.435+0200`." />

  <@lib.parameter name = "dueAfterExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are due after the date described by the given expression.
              See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "dueBefore"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are due before the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
              the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.243+0200`." />

  <@lib.parameter name = "dueBeforeExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are due before the date described by the given expression.
              See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "withoutDueDate"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include tasks which have no due date. Value may only be `true`, 
              as `false` is the default behavior." />

  <@lib.parameter name = "followUpDate"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a followUp date on the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date
              must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.342+0200`." />

  <@lib.parameter name = "followUpDateExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a followUp date on the date described by the given
              expression. See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "followUpAfter"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a followUp date after the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the
              date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.542+0200`." />

  <@lib.parameter name = "followUpAfterExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a followUp date after the date described by the given
              expression. See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "followUpBefore"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a followUp date before the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the
              date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.234+0200`." />

  <@lib.parameter name = "followUpBeforeExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have a followUp date before the date described by the given
              expression. See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "followUpBeforeOrNotExistent"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have no followUp date or a followUp date before the given date.
              By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the
              format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.432+0200`. The
              typical use case is to query all `active` tasks for a user for a given date." />

  <@lib.parameter name = "followUpBeforeOrNotExistentExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that have no followUp date or a followUp date before the date
              described by the given expression. See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "createdOn"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were created on the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
              the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.324+0200`." />

  <@lib.parameter name = "createdOnExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were created on the date described by the given expression.
              See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "createdAfter"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were created after the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the
              format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.342+0200`." />

  <@lib.parameter name = "createdAfterExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were created after the date described by the given expression.
              See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "createdBefore"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were created before the given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the
              format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.332+0200`." />

  <@lib.parameter name = "createdBeforeExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were created before the date described by the given expression.
              See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "updatedAfter"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were updated after the given date. Every action that fires 
              a [task update event](${docsUrl}/user-guide/process-engine/delegation-code/#task-listener-event-lifecycle) is considered as updating the task.
              By [default](${docsUrl}/reference/rest/overview/date-format/), the date must
              have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.332+0200`." />

  <@lib.parameter name = "updatedAfterExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that were updated after the date described by the given expression. Every action that fires 
              a [task update event](${docsUrl}/user-guide/process-engine/delegation-code/#task-listener-event-lifecycle) is considered as updating the task.
              See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to a
              `java.util.Date` or `org.joda.time.DateTime` object." />

  <@lib.parameter name = "delegationState"
      location = "query"
      type = "string"
      enumValues = ['"PENDING"', '"RESOLVED"']
      desc = "Restrict to tasks that are in the given delegation state. Valid values are
              `PENDING` and `RESOLVED`." />

  <@lib.parameter name = "candidateGroups"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are offered to any of the given candidate groups. Takes a
              comma-separated list of group names, so for example `developers,support,sales`." />

  <@lib.parameter name = "candidateGroupsExpression"
      location = "query"
      type = "string"
      desc = "Restrict to tasks that are offered to any of the candidate groups described by the
              given expression. See the
              [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
              for more information on available functions. The expression must evaluate to
              `java.util.List` of Strings." />

  <@lib.parameter name = "withCandidateGroups"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include tasks which have a candidate group. Value may only be `true`,
              as `false` is the default behavior." />

  <@lib.parameter name = "withoutCandidateGroups"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include tasks which have no candidate group. Value may only be `true`,
              as `false` is the default behavior." />

  <@lib.parameter name = "withCandidateUsers"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include tasks which have a candidate user. Value may only be `true`,
              as `false` is the default behavior." />

  <@lib.parameter name = "withoutCandidateUsers"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include tasks which have no candidate users. Value may only be `true`,
              as `false` is the default behavior." />

  <@lib.parameter name = "active"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include active tasks. Value may only be `true`, as `false`
              is the default behavior." />

  <@lib.parameter name = "suspended"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Only include suspended tasks. Value may only be `true`, as
              `false` is the default behavior." />

  <@lib.parameter name = "taskVariables"
      location = "query"
      type = "string"
      desc = "Only include tasks that have variables with certain values. Variable filtering
              expressions are comma-separated and are structured as follows:

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
              `key` and `value` may not contain underscore or comma characters." />

  <@lib.parameter name = "processVariables"
      location = "query"
      type = "string"
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

  <@lib.parameter name = "caseInstanceVariables"
      location = "query"
      type = "string"
      desc = "Only include tasks that belong to case instances that have variables with certain
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
              `like`.
              `key` and `value` may not contain underscore or comma characters." />

  <@lib.parameter name = "variableNamesIgnoreCase"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Match all variable names in this query case-insensitively. If set
              `variableName` and `variablename` are treated as equal." />

  <@lib.parameter name = "variableValuesIgnoreCase"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      desc = "Match all variable values in this query case-insensitively. If set
              `variableValue` and `variablevalue` are treated as equal." />

  <@lib.parameter name = "parentTaskId"
      location = "query"
      type = "string"
      desc = "Restrict query to all tasks that are sub tasks of the given task. Takes a task id." />

  <@lib.parameter name = "withCommentAttachmentInfo"
      location = "query"
      type = "boolean"
      defaultValue = "false"
      last = last
      desc = "Check if task has attachments and/or comments. Value may only be `true`, as
             `false` is the default behavior.
             Adding the filter will do additional attachment and comments queries to the database,
             it might slow down the query in case of tables having high volume of data.
             This param is not considered for count queries" />