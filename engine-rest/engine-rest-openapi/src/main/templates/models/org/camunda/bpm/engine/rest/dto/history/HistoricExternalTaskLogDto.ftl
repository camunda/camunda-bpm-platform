<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/get-external-task-log-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the log entry."
    />
    
    <@lib.property
        name = "externalTaskId"
        type = "string"
        desc = "The id of the external task."
    />
    
    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        desc = "The time when the log entry has been written."
    />
    
    <@lib.property
        name = "topicName"
        type = "string"
        desc = "The topic name of the associated external task."
    />
    
    <@lib.property
        name = "workerId"
        type = "string"
        desc = "The id of the worker that posessed the most recent lock."
    />
    
    <@lib.property
        name = "retries"
        type = "integer"
        format = "int32"
        desc = "The number of retries the associated external task has left."
    />
    
    <@lib.property
        name = "priority"
        type = "integer"
        format = "int64"
        desc = "The execution priority the external task had when the log entry was created."
    />
    
    <@lib.property
        name = "errorMessage"
        type = "string"
        desc = "The message of the error that occurred by executing the associated external task."
    />
    
    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity on which the associated external task was created."
    />
    
    <@lib.property
        name = "activityInstanceId"
        type = "string"
        desc = "The id of the activity instance on which the associated external task was created."
    />
    
    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The execution id on which the associated external task was created."
    />
    
    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance on which the associated external task was created."
    />
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition which the associated external task belongs to."
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition which the associated external task belongs to."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant that this historic external task log entry belongs to."
    />
    
    <@lib.property
        name = "creationLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the creation of the associated
                external task."
    />
    
    <@lib.property
        name = "failureLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the failed execution of the
                associated external task."
    />
    
    <@lib.property
        name = "successLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the successful execution of the
                associated external task."
    />
    
    <@lib.property
        name = "deletionLog"
        type = "boolean"
        desc = "A flag indicating whether this log represents the deletion of the associated
                external task."
    />
    
    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which this log should be removed by the History Cleanup job. Default
                format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`.  For further information, please see the [documentation](${docsUrl}/reference/rest/overview/date-format/)"
    />
    
    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing this log."
        last = true
    />

</@lib.dto>
</#macro>