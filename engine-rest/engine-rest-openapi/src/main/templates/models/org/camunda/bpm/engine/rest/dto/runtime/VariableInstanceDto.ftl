<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/get/index.html -->
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
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition that this variable instance belongs to."
    />
    
    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance that this variable instance belongs to."
    />
    
    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The id of the execution that this variable instance belongs to."
    />
    
    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The id of the case instance that this variable instance belongs to."
    />
    
    <@lib.property
        name = "caseExecutionId"
        type = "string"
        desc = "The id of the case execution that this variable instance belongs to."
    />
    
    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task that this variable instance belongs to."
    />
    
    <@lib.property
        name = "batchId"
        type = "string"
        desc = "The id of the batch that this variable instance belongs to.<"
    />
    
    <@lib.property
        name = "activityInstanceId"
        type = "string"
        desc = "The id of the activity instance that this variable instance belongs to."
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
        last = true
    />

</@lib.dto>
</#macro>