<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/get-job-log-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the log entry."
    />
    
    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        desc = "The time when the log entry has been written."
    />
    
    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the log entry should be removed by the History Cleanup job.
                Default format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`. For further info see the
                [docs](${docsUrl}/reference/rest/overview/date-format/)"
    />
    
    <@lib.property
        name = "jobId"
        type = "string"
        desc = "The id of the associated job."
    />
    
    <@lib.property
        name = "jobDueDate"
        type = "string"
        format = "date-time"
        desc = "The date on which the associated job is supposed to be processed."
    />
    
    <@lib.property
        name = "jobRetries"
        type = "integer"
        format = "int32"
        desc = "The number of retries the associated job has left."
    />
    
    <@lib.property
        name = "jobPriority"
        type = "integer"
        format = "int64"
        desc = "The execution priority the job had when the log entry was created."
    />
    
    <@lib.property
        name = "jobExceptionMessage"
        type = "string"
        desc = "The message of the exception that occurred by executing the associated job."
    />
    
    <@lib.property
        name = "failedActivityId"
        type = "string"
        desc = "The id of the activity on which the last exception occurred by executing the
                associated job."
    />
    
    <@lib.property
        name = "jobDefinitionId"
        type = "string"
        desc = "The id of the job definition on which the associated job was created."
    />
    
    <@lib.property
        name = "jobDefinitionType"
        type = "string"
        desc = "The job definition type of the associated job. See the
                [User Guide](${docsUrl}/user-guide/process-engine/the-job-executor/#job-creation)
                for more information about job definition types."
    />
    
    <@lib.property
        name = "jobDefinitionConfiguration"
        type = "string"
        desc = "The job definition configuration type of the associated job."
    />
    
    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity on which the associated job was created."
    />
    
    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The execution id on which the associated job was created."
    />
    
    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance on which the associated job was created."
    />
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition which the associated job belongs to."
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition which the associated job belongs to."
    />
    
    <@lib.property
        name = "deploymentId"
        type = "string"
        desc = "The id of the deployment which the associated job belongs to."
    />
    
    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                which the associated job belongs to."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant that this historic job log entry belongs to."
    />
    
    <@lib.property
        name = "hostname"
        type = "string"
        desc = "
                The name of the host of the Process Engine where the
                job of this historic job log entry was executed."
    />

    <@lib.property
        name = "batchId"
        type = "string"
        desc = "The ID of the batch associated with this job. `null` if no batch is associated with this job. The
            following jobs are associated with batches:
            * Seed Jobs
            * Monitor Jobs
            * Batch Execution Jobs"
    />

    <@lib.property
        name = "creationLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the creation of the associated job."
    />
    
    <@lib.property
        name = "failureLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the failed execution of the
                associated job."
    />
    
    <@lib.property
        name = "successLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the successful execution of the
                associated job."
    />
    
    <@lib.property
        name = "deletionLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the deletion of the associated job."
        last = true
    />

</@lib.dto>
</#macro>