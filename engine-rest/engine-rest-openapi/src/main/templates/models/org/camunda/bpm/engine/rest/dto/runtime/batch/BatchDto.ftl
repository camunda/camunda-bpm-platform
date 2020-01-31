{
  "type": "object",
  "x-examples": {
    "example-1": {
      "id": "aBatchId",
      "type": "aBatchType",
      "totalJobs": 10,
      "batchJobsPerSeed": 100,
      "jobsCreated": 10,
      "invocationsPerBatchJob": 1,
      "seedJobDefinitionId": "aSeedJobDefinitionId",
      "monitorJobDefinitionId": "aMonitorJobDefinitionId",
      "batchJobDefinitionId": "aBatchJobDefinitionId",
      "suspened": false,
      "tenantId": "aTenantId",
      "createUserId": "demo"
    }
  },
  "properties": {
    "id": {
      "type": "string",
      "description": "The id of the batch."
    },
    "type": {
      "type": "string",
      "description": "The type of the batch."
    },
    "totalJobs": {
      "type": "integer",
      "format": "int32",
      "description": "The total jobs of a batch is the number of batch execution jobs required to complete the batch."
    },
    "jobsCreated": {
      "type": "integer",
      "format": "int32",
      "description": "The number of batch execution jobs already created by the seed job."
    },
    "batchJobsPerSeed": {
      "type": "integer",
      "format": "int32",
      "description": "The number of batch execution jobs created per seed job invocation. The batch seed job is invoked until it has created all batch execution jobs required by the batch (see totalJobs property)."
    },
    "invocationsPerBatchJob": {
      "type": "integer",
      "format": "int32",
      "description": "Every batch execution job invokes the command executed by the batch invocationsPerBatchJob times. E.g., for a process instance migration batch this specifies the number of process instances which are migrated per batch execution job."
    },
    "seedJobDefinitionId": {
      "type": "string",
      "description": "The job definition id for the seed jobs of this batch."
    },
    "monitorJobDefinitionId": {
      "type": "string",
      "description": "The job definition id for the monitor jobs of this batch."
    },
    "batchJobDefinitionId": {
      "type": "string",
      "description": "The job definition id for the batch execution jobs of this batch."
    },
    "suspended": {
      "type": "boolean",
      "description": "Indicates whether this batch is suspended or not."
    },
    "tenantId": {
      "type": "string",
      "description": "The tenant id of the batch."
    },
    "createUserId": {
      "type": "string",
      "description": "The id of the user that created the batch."
    }
  }
}