<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/task/get-task-query/index.html -->

<#assign sortByValues = [
  '"taskId"',
  '"activityInstanceId"',
  '"processDefinitionId"',
  '"processInstanceId"',
  '"executionId"',
  '"duration"',
  '"endTime"',
  '"startTime"',
  '"taskName"',
  '"taskDescription"',
  '"assignee"',
  '"owner"',
  '"dueDate"',
  '"followUpDate"',
  '"deleteReason"',
  '"taskDefinitionKey"',
  '"priority"',
  '"caseDefinitionId"',
  '"caseInstanceId"',
  '"caseExecutionId"',
  '"tenantId"'
]>
<#assign dateFormatDescription = "By [default](${docsUrl}/reference/rest/overview/date-format/),
                                  the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`,
                                  e.g., `2013-01-23T14:42:45.000+0200`."
/>

<#if requestMethod == "GET">
  <#assign listType = "comma-separated">
<#elseif requestMethod == "POST">
  <#assign listType = "">
</#if>
            
<#assign params = {
  "taskId": {
    "type": "string",
    "desc": "Filter by task id."
  },
  "taskParentTaskId": {
    "type": "string",
    "desc": "Filter by parent task id."
  },
  "processInstanceId": {
    "type": "string",
    "desc": "Filter by process instance id."
  },
  "rootProcessInstanceId": {
    "type": "string",
    "desc": "Filter by root process instance id."
  },
  "processInstanceBusinessKey": {
    "type": "string",
    "desc": "Filter by process instance business key."
  },
  "processInstanceBusinessKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by process instances with one of the give business keys.
             The keys need to be in a comma-separated list."
  },
  "processInstanceBusinessKeyLike": {
    "type": "string",
    "desc": "Filter by  process instance business key that has the parameter value as a substring."
  },
  "executionId": {
    "type": "string",
    "desc": "Filter by the id of the execution that executed the task."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Filter by process definition id."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Restrict to tasks that belong to a process definition with the given key."
  },
  "processDefinitionName": {
    "type": "string",
    "desc": "Restrict to tasks that belong to a process definition with the given name."
  },
  "caseInstanceId": {
    "type": "string",
    "desc": "Filter by case instance id."
  },
  "caseExecutionId": {
    "type": "string",
    "desc": "Filter by the id of the case execution that executed the task."
  },
  "caseDefinitionId": {
    "type": "string",
    "desc": "Filter by case definition id."
  },
  "caseDefinitionKey": {
    "type": "string",
    "desc": "Restrict to tasks that belong to a case definition with the given key."
  },
  "caseDefinitionName": {
    "type": "string",
    "desc": "Restrict to tasks that belong to a case definition with the given name."
  },
  "activityInstanceIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include tasks which belong to one of the passed ${listType} activity instance ids."
  },
  "taskName": {
    "type": "string",
    "desc": "Restrict to tasks that have the given name."
  },
  "taskNameLike": {
    "type": "string",
    "desc": "Restrict to tasks that have a name with the given parameter value as substring."
  },
  "taskDescription": {
    "type": "string",
    "desc": "Restrict to tasks that have the given description."
  },
  "taskDescriptionLike": {
    "type": "string",
    "desc": "Restrict to tasks that have a description that has the parameter value as a substring."
  },
  "taskDefinitionKey": {
    "type": "string",
    "desc": "Restrict to tasks that have the given key."
  },
  "taskDefinitionKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Restrict to tasks that have one of the passed ${listType} task definition keys."
  },
  "taskDeleteReason": {
    "type": "string",
    "desc": "Restrict to tasks that have the given delete reason."
  },
  "taskDeleteReasonLike": {
    "type": "string",
    "desc": "Restrict to tasks that have a delete reason that has the parameter value as a substring."
  },
  "taskAssignee": {
    "type": "string",
    "desc": "Restrict to tasks that the given user is assigned to."
  },
  "taskAssigneeLike": {
    "type": "string",
    "desc": "Restrict to tasks that are assigned to users with the parameter value as a substring."
  },
  "taskOwner": {
    "type": "string",
    "desc": "Restrict to tasks that the given user owns."
  },
  "taskOwnerLike": {
    "type": "string",
    "desc": "Restrict to tasks that are owned by users with the parameter value as a substring."
  },
  "taskPriority": {
    "type": "integer",
    "format": "int32",
    "desc": "Restrict to tasks that have the given priority."
  },
  "assigned": {
    "type": "boolean",
    "desc": "If set to `true`, restricts the query to all tasks that are assigned."
  },
  "unassigned": {
    "type": "boolean",
    "desc": "If set to `true`, restricts the query to all tasks that are unassigned."
  },
  "finished": {
    "type": "boolean",
    "desc": "Only include finished tasks. Value may only be `true`, as `false` is the default behavior."
  },
  "unfinished": {
    "type": "boolean",
    "desc": "Only include unfinished tasks. Value may only be `true`, as `false` is the default
             behavior."
  },
  "processFinished": {
    "type": "boolean",
    "desc": "Only include tasks of finished processes. Value may only be `true`, as `false` is the
             default behavior."
  },
  "processUnfinished": {
    "type": "boolean",
    "desc": "Only include tasks of unfinished processes. Value may only be `true`, as `false` is the
             default behavior."
  },
  "taskDueDate": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that are due on the given date. ${dateFormatDescription}"
  },
  "taskDueDateBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that are due before the given date. ${dateFormatDescription}"
  },
  "taskDueDateAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that are due after the given date. ${dateFormatDescription}"
  },
  "withoutTaskDueDate": {
    "type": "boolean",
    "desc": "Only include tasks which have no due date. Value may only be `true`, as `false` is the
             default behavior."
  },
  "taskFollowUpDate": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that have a followUp date on the given date. ${dateFormatDescription}"
  },
  "taskFollowUpDateBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that have a followUp date before the given date. ${dateFormatDescription}"
  },
  "taskFollowUpDateAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that have a followUp date after the given date. ${dateFormatDescription}"
  },
  "startedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that were started before the given date. ${dateFormatDescription}"
  },
  "startedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that were started after the given date. ${dateFormatDescription}"
  },
  "finishedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that were finished before the given date. ${dateFormatDescription}"
  },
  "finishedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Restrict to tasks that were finished after the given date. ${dateFormatDescription}"
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listType} list of tenant ids. A task instance must have one of the given
             tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include historic task instances that belong to no tenant. Value may only be
             `true`, as `false` is the default behavior."
  },
  "taskVariables": {
    "type": "array",
    "dto": "VariableQueryParameterDto",
    "desc": "Only include tasks that have variables with certain values. Variable filtering expressions are
             comma-separated and are structured as follows:

             A valid parameter value has the form `key_operator_value`.
             `key` is the variable name, `operator` is the comparison operator to be used
             and `value` the variable value.
             **Note:** Values are always treated as `String` objects on server side.


             Valid operator values are:
             * `eq` - equal to;
             * `neq` - not equal to;
             * `gt` - greater than;
             * `gteq` - greater than or equal to;
             * `lt` - lower than;
             * `lteq` - lower than or equal to;
             * `like`.

             `key` and `value` may not contain underscore or comma characters."
  },
  "processVariables": {
    "type": "array",
    "dto": "VariableQueryParameterDto",
    "desc": "Only include tasks that belong to process instances that have variables with certain
             values. Variable filtering expressions are comma-separated and are structured as
             follows:

             A valid parameter value has the form `key_operator_value`.
             `key` is the variable name, `operator` is the comparison operator to be used
             and `value` the variable value.
             **Note:** Values are always treated as `String` objects on server side.


             Valid operator values are:
             * `eq` - equal to;
             * `neq` - not equal to;
             * `gt` - greater than;
             * `gteq` - greater than or equal to;
             * `lt` - lower than;
             * `lteq` - lower than or equal to;
             * `like`;
             * `notLike`.

             `key` and `value` may not contain underscore or comma characters."
  },
  "variableNamesIgnoreCase": {
    "type": "boolean",
    "desc": "Match the variable name provided in `taskVariables` and `processVariables` case-
             insensitively. If set to `true` **variableName** and **variablename** are
             treated as equal."
  },
  "variableValuesIgnoreCase": {
    "type": "boolean",
    "desc": "Match the variable value provided in `taskVariables` and `processVariables` case-
             insensitively. If set to `true` **variableValue** and **variablevalue** are
             treated as equal."
  },
  "taskInvolvedUser": {
    "type": "string",
    "desc": "Restrict to tasks with a historic identity link to the given user."
  },
  "taskInvolvedGroup": {
    "type": "string",
    "desc": "Restrict to tasks with a historic identity link to the given group."
  },
  "taskHadCandidateUser": {
    "type": "string",
    "desc": "Restrict to tasks with a historic identity link to the given candidate user."
  },
  "taskHadCandidateGroup": {
    "type": "string",
    "desc": "Restrict to tasks with a historic identity link to the given candidate group."
  },
  "withCandidateGroups": {
    "type": "boolean",
    "desc": "Only include tasks which have a candidate group. Value may only be `true`,
             as `false` is the default behavior."
  },
  "withoutCandidateGroups": {
    "type": "boolean",
    "desc": "Only include tasks which have no candidate group. Value may only be `true`,
             as `false` is the default behavior."
  },
  "orQueries": {
    "type": "array",
    "dto": "HistoricTaskInstanceQueryDto",
    "desc": "A JSON array of nested historic task instance queries with OR semantics.

             A task instance matches a nested query if it fulfills at least one of the query's predicates.

             With multiple nested queries, a task instance must fulfill at least one predicate of each query
             ([Conjunctive Normal Form](https://en.wikipedia.org/wiki/Conjunctive_normal_form)).

             All task instance query properties can be used except for: `sorting`, `withCandidateGroups`, ` withoutCandidateGroups`.

             See the [User Guide](${docsUrl}/user-guide/process-engine/process-engine-api/#or-queries) for more information about OR queries."
  }
}>
