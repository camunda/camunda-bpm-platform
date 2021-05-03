<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-cleanable-process-instance-report-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition."
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition."
    />
    
    <@lib.property
        name = "processDefinitionName"
        type = "string"
        desc = "The name of the process definition."
    />
    
    <@lib.property
        name = "processDefinitionVersion"
        type = "integer"
        format = "int32"
        desc = "The version of the process definition."
    />
    
    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        desc = "The history time to live of the process definition."
    />
    
    <@lib.property
        name = "finishedProcessInstanceCount"
        type = "integer"
        format = "int64"
        desc = "The count of the finished historic process instances."
    />
    
    <@lib.property
        name = "cleanableProcessInstanceCount"
        type = "integer"
        format = "int64"
        desc = "The count of the cleanable historic process instances, referring to history time to
                live."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the process definition."
        last = true
    />

</@lib.dto>
</#macro>