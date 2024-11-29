<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/task/get-task-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
  <#assign dateFormatDescription = "Default [format](${docsUrl}/reference/rest/overview/date-format/)
                                    `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
  />
    
  <@lib.property
    name = "id"
    type = "string"
    desc = "The task id."
  />
    
  <@lib.property
      name = "processDefinitionKey"
      type = "string"
      desc = "The key of the process definition the task belongs to."
  />

  <@lib.property
      name = "processDefinitionId"
      type = "string"
      desc = "The id of the process definition the task belongs to."
  />

  <@lib.property
      name = "processInstanceId"
      type = "string"
      desc = "The id of the process instance the task belongs to."
  />

  <@lib.property
      name = "executionId"
      type = "string"
      desc = "The id of the execution the task belongs to."
  />

  <@lib.property
      name = "caseDefinitionKey"
      type = "string"
      desc = "The key of the case definition the task belongs to."
  />

  <@lib.property
      name = "caseDefinitionId"
      type = "string"
      desc = "The id of the case definition the task belongs to."
  />

  <@lib.property
      name = "caseInstanceId"
      type = "string"
      desc = "The id of the case instance the task belongs to."
  />

  <@lib.property
      name = "caseExecutionId"
      type = "string"
      desc = "The id of the case execution the task belongs to."
  />

  <@lib.property
      name = "activityInstanceId"
      type = "string"
      desc = "The id of the activity that this object is an instance of."
  />

  <@lib.property
      name = "name"
      type = "string"
      desc = "The task name."
  />

  <@lib.property
      name = "description"
      type = "string"
      desc = "The task's description."
  />

  <@lib.property
      name = "deleteReason"
      type = "string"
      desc = "The task's delete reason."
  />

  <@lib.property
      name = "owner"
      type = "string"
      desc = "The owner's id."
  />

  <@lib.property
      name = "assignee"
      type = "string"
      desc = "The assignee's id."
  />

  <@lib.property
      name = "startTime"
      type = "string"
      format = "date-time"
      desc = "The time the task was started. ${dateFormatDescription}"
  />

  <@lib.property
      name = "endTime"
      type = "string"
      format = "date-time"
      desc = "The time the task ended. ${dateFormatDescription}"
  />

  <@lib.property
      name = "duration"
      type = "integer"
      format = "int64"
      desc = "The time the task took to finish (in milliseconds)."
  />

  <@lib.property
      name = "taskDefinitionKey"
      type = "string"
      desc = "The task's key."
  />

  <@lib.property
      name = "priority"
      type = "integer"
      format = "int32"
      desc = "The task's priority."
  />

  <@lib.property
      name = "due"
      type = "string"
      format = "date-time"
      desc = "The task's due date. ${dateFormatDescription}"
  />

  <@lib.property
      name = "parentTaskId"
      type = "string"
      desc = "The id of the parent task, if this task is a subtask."
  />

  <@lib.property
      name = "followUp"
      type = "string"
      format = "date-time"
      desc = "The follow-up date for the task. ${dateFormatDescription}"
  />

  <@lib.property
      name = "tenantId"
      type = "string"
      desc = "The tenant id of the task instance."
  />

  <@lib.property
      name = "removalTime"
      type = "string"
      format = "date-time"
      desc = "The time after which the task should be removed by the History Cleanup job. ${dateFormatDescription}"
  />

  <@lib.property
      name = "rootProcessInstanceId"
      type = "string"
      desc = "The process instance id of the root process instance that initiated the process
              containing this task."
  />

  <@lib.property
    name = "taskState"
    type = "string"
    desc = "The task's state."
    last = true
  />

</@lib.dto>
</#macro>