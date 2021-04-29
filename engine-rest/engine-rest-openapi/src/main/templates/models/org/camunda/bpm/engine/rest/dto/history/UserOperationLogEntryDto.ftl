<#macro dto_macro docsUrl="">
<@lib.dto >
    
  <@lib.property
      name = "id"
      type = "string"
      desc = "The unique identifier of this log entry."
  />

  <@lib.property
      name = "userId"
      type = "string"
      desc = "The user who performed this operation."
  />

  <@lib.property
      name = "timestamp"
      type = "string"
      format = "date-time"
      desc = "Timestamp of this operation."
  />

  <@lib.property
      name = "operationId"
      type = "string"
      desc = "The unique identifier of this operation. A composite operation that changes
              multiple properties has a common `operationId`."
  />

  <@lib.property
      name = "operationType"
      type = "string"
      desc = "The type of this operation, e.g., `Assign`, `Claim` and so on."
  />

  <@lib.property
      name = "entityType"
      type = "string"
      desc = "The type of the entity on which this operation was executed, e.g., `Task` or
              `Attachment`."
  />

  <@lib.property
      name = "category"
      type = "string"
      desc = "The name of the category this operation was associated with, e.g., `TaskWorker` or
              `Admin`."
  />

  <@lib.property
      name = "annotation"
      type = "string"
      desc = "An arbitrary annotation set by a user for auditing reasons."
  />

  <@lib.property
      name = "property"
      type = "string"
      desc = "The property changed by this operation."
  />

  <@lib.property
      name = "orgValue"
      type = "string"
      desc = "The original value of the changed property."
  />

  <@lib.property
      name = "newValue"
      type = "string"
      desc = "The new value of the changed property."
  />

  <@lib.property
      name = "deploymentId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this
              deployment."
  />

  <@lib.property
      name = "processDefinitionId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this process
              definition."
  />

  <@lib.property
      name = "processDefinitionKey"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to process
              definitions with this key."
  />

  <@lib.property
      name = "processInstanceId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this process
              instance."
  />

  <@lib.property
      name = "executionId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this
              execution."
  />

  <@lib.property
      name = "caseDefinitionId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this case
              definition."
  />

  <@lib.property
      name = "caseInstanceId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this case
              instance."
  />

  <@lib.property
      name = "caseExecutionId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this case
              execution."
  />

  <@lib.property
      name = "taskId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this task."
  />

  <@lib.property
      name = "externalTaskId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this external task."
  />

  <@lib.property
      name = "batchId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this batch."
  />

  <@lib.property
      name = "jobId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this job."
  />

  <@lib.property
      name = "jobDefinitionId"
      type = "string"
      desc = "If not `null`, the operation is restricted to entities in relation to this job
              definition."
  />

  <@lib.property
      name = "removalTime"
      type = "string"
      format = "date-time"
      desc = "The time after which the entry should be removed by the History Cleanup job.
              [Default format](${docsUrl}/reference/rest/overview/date-format/)
              `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
  />

  <@lib.property
      name = "rootProcessInstanceId"
      type = "string"
      desc = "The process instance id of the root process instance that initiated the process
              containing this entry."
      last = true
  />

</@lib.dto>
</#macro>