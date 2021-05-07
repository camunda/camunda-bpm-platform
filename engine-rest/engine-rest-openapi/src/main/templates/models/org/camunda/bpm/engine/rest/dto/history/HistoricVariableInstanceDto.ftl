<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/get-variable-instance-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto extends="VariableValueDto">
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the variable instance."
    />
    
    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the variable instance."
    />


    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition the variable instance belongs to."
    />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition the variable instance belongs to."
    />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The process instance id the variable instance belongs to."
    />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The execution id the variable instance belongs to."
    />

    <@lib.property
        name = "activityInstanceId"
        type = "string"
        desc = "The id of the activity instance in which the variable is valid."
    />

    <@lib.property
        name = "caseDefinitionKey"
        type = "string"
        desc = "The key of the case definition the variable instance belongs to."
    />

    <@lib.property
        name = "caseDefinitionId"
        type = "string"
        desc = "The id of the case definition the variable instance belongs to."
    />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The case instance id the variable instance belongs to."
    />

    <@lib.property
        name = "caseExecutionId"
        type = "string"
        desc = "The case execution id the variable instance belongs to."
    />

    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task the variable instance belongs to."
    />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant that this variable instance belongs to."
    />

    <@lib.property
        name = "errorMessage"
        type = "string"
        desc = "An error message in case a Java Serialized Object could not be de-serialized."
    />

    <@lib.property
        name = "state"
        type = "string"
        desc = "The current state of the variable. Can be 'CREATED' or 'DELETED'."
    />

    <@lib.property
        name = "createTime"
        type = "string"
        format = "date-time"
        desc = "The time the variable was inserted. [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />

    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the variable should be removed by the History Cleanup job.
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />

    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing this variable."
        last = true
    />

</@lib.dto>
</#macro>
