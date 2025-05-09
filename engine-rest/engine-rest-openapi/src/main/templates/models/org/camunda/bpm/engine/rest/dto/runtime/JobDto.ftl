<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/get/index.html -->
<@lib.dto>
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the job."
    />
    
    <@lib.property
        name = "jobDefinitionId"
        type = "string"
        desc = "The id of the associated job definition."
    />
    
    <@lib.property
        name = "dueDate"
        type = "string"
        format = "date-time"
        desc = "The date on which this job is supposed to be processed."
    />
    
    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance which execution created the job."
    />
    
    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The specific execution id on which the job was created."
    />
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition which this job belongs to."
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition which this job belongs to."
    />
    
    <@lib.property
        name = "retries"
        type = "integer"
        format = "int32"
        desc = "The number of retries this job has left."
    />
    
    <@lib.property
        name = "exceptionMessage"
        type = "string"
        desc = "The message of the exception that occurred, the last time the job was executed. Is
                null when no exception occurred."
    />
    
    <@lib.property
        name = "failedActivityId"
        type = "string"
        desc = "The id of the activity on which the last exception occurred, the last time the job
                was executed. Is null when no exception occurred."
    />
    
    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A flag indicating whether the job is suspended or not."
    />
    
    <@lib.property
        name = "priority"
        type = "integer"
        format = "int64"
        desc = "The job's priority for execution."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant which this job belongs to."
    />
    
    <@lib.property
        name = "createTime"
        type = "string"
        format = "date-time"
        desc = "The date on which this job has been created."
    />

    <@lib.property
        name = "batchId"
        type = "string"
        desc = "The ID of the batch associated with this job. `null` if no batch is associated with this job. The
        following jobs are associated with batches:
        * Seed Jobs
        * Monitor Jobs
        * Batch Execution Jobs"
        last = true
    />

</@lib.dto>
</#macro>