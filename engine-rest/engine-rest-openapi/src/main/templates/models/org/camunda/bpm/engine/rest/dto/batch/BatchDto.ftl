<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the batch." />

    <@lib.property
        name = "type"
        type = "string"
        desc = "The type of the batch.
                See the [User Guide](${docsUrl}/user-guide/process-engine/batch/#creating-a-batch)
                for more information about batch types." />

    <@lib.property
        name = "totalJobs"
        type = "integer"
        format = "int32"
        desc = "The total jobs of a batch is the number of batch execution jobs required to complete the batch." />

    <@lib.property
        name = "jobsCreated"
        type = "integer"
        format = "int32"
        desc = "The number of batch execution jobs already created by the seed job." />

    <@lib.property
        name = "batchJobsPerSeed"
        type = "integer"
        format = "int32"
        desc = "The number of batch execution jobs created per seed job invocation.
                The batch seed job is invoked until it has created all batch execution jobs required by the batch
                (see `totalJobs` property)." />

    <@lib.property
        name = "invocationsPerBatchJob"
        type = "integer"
        format = "int32"
        desc = "Every batch execution job invokes the command executed by the batch `invocationsPerBatchJob` times.
                E.g., for a process instance migration batch this specifies the number of process instances which are migrated per batch execution job." />

    <@lib.property
        name = "seedJobDefinitionId"
        type = "string"
        desc = "The job definition id for the seed jobs of this batch." />

    <@lib.property
        name = "monitorJobDefinitionId"
        type = "string"
        desc = "The job definition id for the monitor jobs of this batch." />

    <@lib.property
        name = "batchJobDefinitionId"
        type = "string"
        desc = "The job definition id for the batch execution jobs of this batch." />

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "Indicates whether this batch is suspended or not." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the batch." />

    <@lib.property
        name = "createUserId"
        type = "string"
        desc = "The id of the user that created the batch." />

    <@lib.property
        name = "startTime"
        type = "string"
        format = "date-time"
        desc = "The time the batch was started. Default format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`. For further information,
         please see the [documentation] (${docsUrl}/reference/rest/overview/date-format/)" />

    <@lib.property
        name = "executionStartTime"
        type = "string"
        format = "date-time"
        last = true
        desc = "The time the batch execution was started, i.e., at least one batch job has been executed. Default
        format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`. For further information, please see the [documentation]
        (${docsUrl}/reference/rest/overview/date-format/)" />

</@lib.dto>
</#macro>