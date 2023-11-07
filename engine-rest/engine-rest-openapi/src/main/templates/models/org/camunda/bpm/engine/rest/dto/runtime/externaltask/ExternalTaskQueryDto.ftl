<#macro dto_macro docsUrl="">
<@lib.dto
    title = "ExternalTaskQueryDto"
    desc = "A JSON object with the following properties:" >

  <@lib.property
      name = "externalTaskId"
      type = "string"
      desc = "Filter by an external task's id." />

  <@lib.property
    name = "externalTaskIdIn"
    type = "array"
    itemType = "string"
    desc = "Filter by the comma-separated list of external task ids." />

  <@lib.property
      name = "topicName"
      type = "string"
      desc = "Filter by an external task topic." />

  <@lib.property
      name = "workerId"
      type = "string"
      desc = "Filter by the id of the worker that the task was most recently locked by." />

  <@lib.property
      name = "locked"
      type = "boolean"
      desc = "Only include external tasks that are currently locked (i.e., they have a lock time and it has not expired).
              Value may only be `true`, as `false` matches any external task." />

  <@lib.property
      name = "notLocked"
      type = "boolean"
      desc = "Only include external tasks that are currently not locked (i.e., they have no lock or it has expired).
              Value may only be `true`, as `false` matches any external task." />

  <@lib.property
      name = "withRetriesLeft"
      type = "boolean"
      desc = "Only include external tasks that have a positive (&gt; 0) number of retries (or `null`). Value may only be
              `true`, as `false` matches any external task." />

  <@lib.property
      name = "noRetriesLeft"
      type = "boolean"
      desc = "Only include external tasks that have 0 retries. Value may only be `true`, as `false` matches any
              external task." />

  <@lib.property
      name = "lockExpirationAfter"
      type = "string"
      format = "date-time"
      desc = "Restrict to external tasks that have a lock that expires after a given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
              `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

  <@lib.property
      name = "lockExpirationBefore"
      type = "string"
      format = "date-time"
      desc = "Restrict to external tasks that have a lock that expires before a given date. By
              [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
              `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

  <@lib.property
      name = "activityId"
      type = "string"
      desc = "Filter by the id of the activity that an external task is created for." />

  <@lib.property
      name = "activityIdIn"
      type = "array"
      itemType = "string"
      desc = "Filter by the comma-separated list of ids of the activities that an external task is created for." />

  <@lib.property
      name = "executionId"
      type = "string"
      desc = "Filter by the id of the execution that an external task belongs to." />

  <@lib.property
      name = "processInstanceId"
      type = "string"
      desc = "Filter by the id of the process instance that an external task belongs to." />

  <@lib.property
      name = "processInstanceIdIn"
      type = "array"
      itemType = "string"
      desc = "Filter by a comma-separated list of process instance ids that an external task may belong to." />

  <@lib.property
      name = "processDefinitionId"
      type = "string"
      desc = "Filter by the id of the process definition that an external task belongs to." />

  <@lib.property
      name = "tenantIdIn"
      type = "array"
      itemType = "string"
      desc = "Filter by a comma-separated list of tenant ids.
              An external task must have one of the given tenant ids." />

  <@lib.property
      name = "active"
      type = "boolean"
      desc = "Only include active tasks. Value may only be `true`, as `false` matches any external task." />

  <@lib.property
      name = "suspended"
      type = "boolean"
      desc = "Only include suspended tasks. Value may only be `true`, as `false` matches any external task." />

  <@lib.property
      name = "priorityHigherThanOrEquals"
      type = "integer"
      format = "int64"
      desc = "Only include jobs with a priority higher than or equal to the given value.
              Value must be a valid `long` value." />

  <@lib.property
      name = "priorityLowerThanOrEquals"
      type = "integer"
      format = "int64"
      desc = "Only include jobs with a priority lower than or equal to the given value.
              Value must be a valid `long` value." />

  "sorting": {
    "type": "array",
    "nullable": true,
    "description": "A JSON array of criteria to sort the result by. Each element of the array is a JSON object that
                    specifies one ordering. The position in the array identifies the rank of an ordering, i.e., whether
                    it is primary, secondary, etc. The ordering objects have the following properties:

                    **Note:** The `sorting` properties will not be applied to the External Task count query.",
    "items":

    <#assign last = true>
    <#assign sortByValues = [ '"id"', '"lockExpirationTime"', '"processInstanceId"', '"processDefinitionId"',
                              '"processDefinitionKey"', '"taskPriority"', '"createTime"', '"tenantId"' ] >
    <#include "/lib/commons/sort-props.ftl" >
    }

</@lib.dto>

</#macro>