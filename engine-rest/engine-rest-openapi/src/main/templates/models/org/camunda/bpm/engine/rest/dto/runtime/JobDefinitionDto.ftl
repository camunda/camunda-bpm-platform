<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/get-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the job definition."
    />
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition this job definition is associated with."
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition this job definition is associated with."
    />
    
    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity this job definition is associated with."
    />
    
    <@lib.property
        name = "jobType"
        type = "string"
        desc = "The type of the job which is running for this job definition. See the
                [User Guide](${docsUrl}/user-guide/process-engine/the-job-executor/#job-creation)
                for more information about job types."
    />
    
    <@lib.property
        name = "jobConfiguration"
        type = "string"
        desc = "The configuration of a job definition provides details about the jobs which will be
                created. For example: for timer jobs it is the timer configuration."
    />
    
    <@lib.property
        name = "overridingJobPriority"
        type = "integer"
        format = "int64"
        desc = "The execution priority defined for jobs that are created based on this definition.
                May be `null` when the priority has not been overridden on the job
                definition level."
    />
    
    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "Indicates whether this job definition is suspended or not."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant this job definition is associated with."
    />
    
    <@lib.property
        name = "deploymentId"
        type = "string"
        desc = "The id of the deployment this job definition is related to. In a deployment-aware
                setup, this leads to all jobs of the same definition being executed
                on the same node."
        last = true
    />

</@lib.dto>
</#macro>