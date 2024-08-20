<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "A Task query which defines a group of Tasks." >

    <@lib.property
        name = "taskId"
        type = "string"
        desc = "Restrict to task with the given id." />

    <@lib.property
        name = "taskIdIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to tasks with any of the given ids." />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "Restrict to tasks that belong to process instances with the given id." />
  
    <@lib.property
        name = "processInstanceIdIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to tasks that belong to process instances with the given ids." />
  
    <@lib.property
        name = "processInstanceBusinessKey"
        type = "string"
        desc = "Restrict to tasks that belong to process instances with the given business key." />
  
    <@lib.property
        name = "processInstanceBusinessKeyExpression"
        type = "string"
        desc = "Restrict to tasks that belong to process instances with the given business key which 
                is described by an expression. See the 
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions." />
  
    <@lib.property
        name = "processInstanceBusinessKeyIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to tasks that belong to process instances with one of the give business keys. 
                The keys need to be in a comma-separated list." />
  
    <@lib.property
        name = "processInstanceBusinessKeyLike"
        type = "string"
        desc = "Restrict to tasks that have a process instance business key that has the parameter 
                value as a substring." />
  
    <@lib.property
        name = "processInstanceBusinessKeyLikeExpression"
        type = "string"
        desc = "Restrict to tasks that have a process instance business key that has the parameter 
                value as a substring and is described by an expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
                for more information on available functions." />
  
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "Restrict to tasks that belong to a process definition with the given id." />
  
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "Restrict to tasks that belong to a process definition with the given key." />
  
    <@lib.property
        name = "processDefinitionKeyIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to tasks that belong to a process definition with one of the given keys. The 
                keys need to be in a comma-separated list." />
  
    <@lib.property
        name = "processDefinitionName"
        type = "string"
        desc = "Restrict to tasks that belong to a process definition with the given name." />
  
    <@lib.property
        name = "processDefinitionNameLike"
        type = "string"
        desc = "Restrict to tasks that have a process definition name that has the parameter value as 
                a substring." />
  
    <@lib.property
        name = "executionId"
        type = "string"
        desc = "Restrict to tasks that belong to an execution with the given id." />
  
    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "Restrict to tasks that belong to case instances with the given id." />
  
    <@lib.property
        name = "caseInstanceBusinessKey"
        type = "string"
        desc = "Restrict to tasks that belong to case instances with the given business key." />
  
    <@lib.property
        name = "caseInstanceBusinessKeyLike"
        type = "string"
        desc = "Restrict to tasks that have a case instance business key that has the parameter value 
                as a substring." />
  
    <@lib.property
        name = "caseDefinitionId"
        type = "string"
        desc = "Restrict to tasks that belong to a case definition with the given id." />
  
    <@lib.property
        name = "caseDefinitionKey"
        type = "string"
        desc = "Restrict to tasks that belong to a case definition with the given key." />
  
    <@lib.property
        name = "caseDefinitionName"
        type = "string"
        desc = "Restrict to tasks that belong to a case definition with the given name." />
  
    <@lib.property
        name = "caseDefinitionNameLike"
        type = "string"
        desc = "Restrict to tasks that have a case definition name that has the parameter value as a 
                substring." />
  
    <@lib.property
        name = "caseExecutionId"
        type = "string"
        desc = "Restrict to tasks that belong to a case execution with the given id." />
  
    <@lib.property
        name = "activityInstanceIdIn"
        type = "array"
        itemType = "string"
        desc = "Only include tasks which belong to one of the passed and comma-separated activity 
                instance ids." />
  
    <@lib.property
        name = "tenantIdIn"
        type = "array"
        itemType = "string"
        desc = "Only include tasks which belong to one of the passed and comma-separated 
                tenant ids." />
  
    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include tasks which belong to no tenant. Value may only be `true`, 
                as `false` is the default behavior." />
  
    <@lib.property
        name = "assignee"
        type = "string"
        desc = "Restrict to tasks that the given user is assigned to." />
  
    <@lib.property
        name = "assigneeExpression"
        type = "string"
        desc = "Restrict to tasks that the user described by the given expression is assigned to. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
                for more information on available functions." />
  
    <@lib.property
        name = "assigneeLike"
        type = "string"
        desc = "Restrict to tasks that have an assignee that has the parameter 
                value as a substring." />
  
    <@lib.property
        name = "assigneeLikeExpression"
        type = "string"
        desc = "Restrict to tasks that have an assignee that has the parameter value described by the 
                given expression as a substring. See the 
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
                for more information on available functions." />
  
    <@lib.property
        name = "assigneeIn"
        type = "array"
        itemType = "string"
        desc = "Only include tasks which are assigned to one of the passed and comma-separated user ids." />

    <@lib.property
        name = "assigneeNotIn"
        type = "array"
        itemType = "string"
        desc = "Only include tasks which are not assigned to one of the passed and comma-separated user ids." />
  
    <@lib.property
        name = "owner"
        type = "string"
        desc = "Restrict to tasks that the given user owns." />
  
    <@lib.property
        name = "ownerExpression"
        type = "string"
        desc = "Restrict to tasks that the user described by the given expression owns. See the 
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
                for more information on available functions." />
  
    <@lib.property
        name = "candidateGroup"
        type = "string"
        desc = "Only include tasks that are offered to the given group." />

    <@lib.property
    name = "candidateGroupLike"
    type = "string"
    desc = "Only include tasks that are offered to groups that have the parameter value as a substring." />
  
    <@lib.property
        name = "candidateGroupExpression"
        type = "string"
        desc = "Only include tasks that are offered to the group described by the given expression. 
                See the 
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
                for more information on available functions." />
  
    <@lib.property
        name = "candidateUser"
        type = "string"
        desc = "Only include tasks that are offered to the given user or to one of his groups." />
  
    <@lib.property
        name = "candidateUserExpression"
        type = "string"
        desc = "Only include tasks that are offered to the user described by the given expression. 
                See the 
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions) 
                for more information on available functions." />
  
    <@lib.property
        name = "includeAssignedTasks"
        type = "boolean"
        defaultValue = "false"
        desc = "Also include tasks that are assigned to users in candidate queries. Default is to only 
                include tasks that are not assigned to any user if you query by candidate user or
                group(s)." />
  
    <@lib.property
        name = "involvedUser"
        type = "string"
        desc = "Only include tasks that the given user is involved in. A user is involved in a task if 
                an identity link exists between task and user (e.g., the user is the assignee)." />
  
    <@lib.property
        name = "involvedUserExpression"
        type = "string"
        desc = "Only include tasks that the user described by the given expression is involved in.
                A user is involved in a task if an identity link exists between task and user
                (e.g., the user is the assignee). See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions." />
  
    <@lib.property
        name = "assigned"
        type = "boolean"
        defaultValue = "false"
        desc = "If set to `true`, restricts the query to all tasks that are assigned." />
  
    <@lib.property
        name = "unassigned"
        type = "boolean"
        defaultValue = "false"
        desc = "If set to `true`, restricts the query to all tasks that are unassigned." />
  
    <@lib.property
        name = "taskDefinitionKey"
        type = "string"
        desc = "Restrict to tasks that have the given key." />
  
    <@lib.property
        name = "taskDefinitionKeyIn"
        type = "array"
        itemType = "string"
        desc = "Restrict to tasks that have one of the given keys. The keys need to be in a comma-separated list." />
  
    <@lib.property
        name = "taskDefinitionKeyLike"
        type = "string"
        desc = "Restrict to tasks that have a key that has the parameter value as a substring." />
  
    <@lib.property
        name = "name"
        type = "string"
        desc = "Restrict to tasks that have the given name." />
  
    <@lib.property
        name = "nameNotEqual"
        type = "string"
        desc = "Restrict to tasks that do not have the given name." />
  
    <@lib.property
        name = "nameLike"
        type = "string"
        desc = "Restrict to tasks that have a name with the given parameter value as substring." />
  
    <@lib.property
        name = "nameNotLike"
        type = "string"
        desc = "Restrict to tasks that do not have a name with the given parameter
                value as substring." />
  
    <@lib.property
        name = "description"
        type = "string"
        desc = "Restrict to tasks that have the given description." />
  
    <@lib.property
        name = "descriptionLike"
        type = "string"
        desc = "Restrict to tasks that have a description that has the parameter
                value as a substring." />
  
    <@lib.property
        name = "priority"
        type = "integer"
        format = "int32"
        desc = "Restrict to tasks that have the given priority." />
  
    <@lib.property
        name = "maxPriority"
        type = "integer"
        format = "int32"
        desc = "Restrict to tasks that have a lower or equal priority." />
  
    <@lib.property
        name = "minPriority"
        type = "integer"
        format = "int32"
        desc = "Restrict to tasks that have a higher or equal priority." />
  
    <@lib.property
        name = "dueDate"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that are due on the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.546+0200`." />
  
    <@lib.property
        name = "dueDateExpression"
        type = "string"
        desc = "Restrict to tasks that are due on the date described by the given expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "dueAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that are due after the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
                the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.435+0200`." />
  
    <@lib.property
        name = "dueAfterExpression"
        type = "string"
        desc = "Restrict to tasks that are due after the date described by the given expression.
                See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "dueBefore"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that are due before the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
                the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.243+0200`." />
  
    <@lib.property
        name = "dueBeforeExpression"
        type = "string"
        desc = "Restrict to tasks that are due before the date described by the given expression.
                See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "withoutDueDate"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include tasks which have no due date. Value may only be `true`, 
                as `false` is the default behavior." />
  
    <@lib.property
        name = "followUpDate"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that have a followUp date on the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date
                must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.342+0200`." />
  
    <@lib.property
        name = "followUpDateExpression"
        type = "string"
        desc = "Restrict to tasks that have a followUp date on the date described by the given
                expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "followUpAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that have a followUp date after the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
                the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.542+0200`." />
  
    <@lib.property
        name = "followUpAfterExpression"
        type = "string"
        desc = "Restrict to tasks that have a followUp date after the date described by the given
                expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "followUpBefore"
        type = "string"
        desc = "Restrict to tasks that have a followUp date before the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
                the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.234+0200`." />
  
    <@lib.property
        name = "followUpBeforeExpression"
        type = "string"
        desc = "Restrict to tasks that have a followUp date before the date described by the given
                expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "followUpBeforeOrNotExistent"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that have no followUp date or a followUp date before the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
                the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.432+0200`. The typical use case
                is to query all `active` tasks for a user for a given date." />
  
    <@lib.property
        name = "followUpBeforeOrNotExistentExpression"
        type = "string"
        desc = "Restrict to tasks that have no followUp date or a followUp date before the date
                described by the given expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "createdOn"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that were created on the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must have
                the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.324+0200`." />
  
    <@lib.property
        name = "createdOnExpression"
        type = "string"
        desc = "Restrict to tasks that were created on the date described by the given expression.
                See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "createdAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that were created after the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must
                have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.342+0200`." />
  
    <@lib.property
        name = "createdAfterExpression"
        type = "string"
        desc = "Restrict to tasks that were created after the date described by the given expression.
                See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "createdBefore"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that were created before the given date. By
                [default](${docsUrl}/reference/rest/overview/date-format/), the date must
                have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.332+0200`." />
  
    <@lib.property
        name = "createdBeforeExpression"
        type = "string"
        desc = "Restrict to tasks that were created before the date described by the given expression.
                See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "updatedAfter"
        type = "string"
        format = "date-time"
        desc = "Restrict to tasks that were updated after the given date. Every action that fires 
                a [task update event](${docsUrl}/user-guide/process-engine/delegation-code/#task-listener-event-lifecycle) is considered as updating the task.
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date must
                have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.332+0200`." />
  
    <@lib.property
        name = "updatedAfterExpression"
        type = "string"
        desc = "Restrict to tasks that were updated after the date described by the given expression. Every action that fires 
                a [task update event](${docsUrl}/user-guide/process-engine/delegation-code/#task-listener-event-lifecycle) is considered as updating the task.
                See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to a
                `java.util.Date` or `org.joda.time.DateTime` object." />
  
    <@lib.property
        name = "delegationState"
        type = "string"
        enumValues = ['"PENDING"', '"RESOLVED"']
        desc = "Restrict to tasks that are in the given delegation state. Valid values are
                `PENDING` and `RESOLVED`." />
  
    <@lib.property
        name = "candidateGroups"
        type = "array"
        itemType = "string"
        desc = "Restrict to tasks that are offered to any of the given candidate groups. Takes a
                comma-separated list of group names, so for example
                `developers,support,sales`." />
  
    <@lib.property
        name = "candidateGroupsExpression"
        type = "string"
        desc = "Restrict to tasks that are offered to any of the candidate groups described by the
                given expression. See the
                [user guide](${docsUrl}/user-guide/process-engine/expression-language/#internal-context-functions)
                for more information on available functions. The expression must evaluate to
                `java.util.List` of Strings." />
  
    <@lib.property
        name = "withCandidateGroups"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include tasks which have a candidate group. Value may only be `true`,
                as `false` is the default behavior." />
  
    <@lib.property
        name = "withoutCandidateGroups"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include tasks which have no candidate group. Value may only be `true`,
                as `false` is the default behavior." />
  
    <@lib.property
        name = "withCandidateUsers"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include tasks which have a candidate user. Value may only be `true`,
                as `false` is the default behavior." />
  
    <@lib.property
        name = "withoutCandidateUsers"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include tasks which have no candidate users. Value may only be `true`,
                as `false` is the default behavior." />
  
    <@lib.property
        name = "active"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include active tasks. Value may only be `true`, as `false`
                is the default behavior." />
  
    <@lib.property
        name = "suspended"
        type = "boolean"
        defaultValue = "false"
        desc = "Only include suspended tasks. Value may only be `true`, as
                `false` is the default behavior." />
  
    <@lib.property
        name = "taskVariables"
        type = "array"
        dto = "VariableQueryParameterDto"
        desc = "A JSON array to only include tasks that have variables with certain values. The
                array consists of JSON objects with three properties `name`, `operator` and `value`.
                `name` is the variable name, `operator` is the comparison operator to be used and
                `value` the variable value. `value` may be of type `String`, `Number` or `Boolean`.
  
                Valid `operator` values are:
                `eq` - equal to;
                `neq` - not equal to;
                `gt` - greater than;
                `gteq` - greater than or equal to;
                `lt` - lower than;
                `lteq` - lower than or equal to;
                `like`.
                `key` and `value` may not contain underscore or comma characters." />
  
    <@lib.property
        name = "processVariables"
        type = "array"
        dto = "VariableQueryParameterDto"
        desc = "A JSON array to only include tasks that belong to a process instance with variables
                with certain values. The array consists of JSON objects with three properties
                `name`, `operator` and `value`. `name` is the variable name, `operator` is the
                comparison operator to be used and `value` the variable value. `value` may be of
                type `String`, `Number` or `Boolean`.
  
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
  
    <@lib.property
        name = "caseInstanceVariables"
        type = "array"
        dto = "VariableQueryParameterDto"
        desc = "A JSON array to only include tasks that belong to a case instance with variables
                with certain values. The array consists of JSON objects with three properties
                `name`, `operator` and `value`. `name` is the variable name, `operator` is the
                comparison operator to be used and `value` the variable value. `value` may be of
                type `String`, `Number` or `Boolean`.
  
                Valid `operator` values are:
                `eq` - equal to;
                `neq` - not equal to;
                `gt` - greater than;
                `gteq` - greater than or equal to;
                `lt` - lower than;
                `lteq` - lower than or equal to;
                `like`.
                `key` and `value` may not contain underscore or comma characters." />
  
    <@lib.property
        name = "variableNamesIgnoreCase"
        type = "boolean"
        defaultValue = "false"
        desc = "Match all variable names in this query case-insensitively. If set
                `variableName` and `variablename` are treated as equal." />
  
    <@lib.property
        name = "variableValuesIgnoreCase"
        type = "boolean"
        defaultValue = "false"
        desc = "Match all variable values in this query case-insensitively. If set
                `variableValue` and `variablevalue` are treated as equal." />
  
    <@lib.property
        name = "parentTaskId"
        type = "string"
        desc = "Restrict query to all tasks that are sub tasks of the given task. Takes a task id." />

    <@lib.property
        name = "orQueries"
        type = "array"
        dto = "TaskQueryDto"
        desc = "A JSON array of nested task queries with OR semantics. A task matches a nested query if it fulfills
                *at least one* of the query's predicates. With multiple nested queries, a task must fulfill at least one predicate of *each* query ([Conjunctive Normal Form](https://en.wikipedia.org/wiki/Conjunctive_normal_form)).

                All task query properties can be used except for: `sorting`, `withCandidateGroups`,
    `withoutCandidateGroups`, `withCandidateUsers`, `withoutCandidateUsers`

                See the [User guide](${docsUrl}/user-guide/process-engine/process-engine-api/#or-queries)
    for more information about OR queries." />

    "sorting": {
      "type": "array",
      "nullable": true,
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#assign sortByValues = [ '"instanceId"', '"caseInstanceId"', '"dueDate"', '"executionId"', '"caseExecutionId"',
                                  '"assignee"', '"created"', '"lastUpdated"', '"followUpDate"', '"description"', '"id"', '"name"', '"nameCaseInsensitive"',
                                  '"priority"', '"processVariable"', '"executionVariable"', '"taskVariable"',
                                  '"caseExecutionVariable"', '"caseInstanceVariable"' ] >
        <#assign sortParamsDto = "SortTaskQueryParametersDto" >
        <#include "/lib/commons/sort-props.ftl" >
    }

</@lib.dto>
</#macro>