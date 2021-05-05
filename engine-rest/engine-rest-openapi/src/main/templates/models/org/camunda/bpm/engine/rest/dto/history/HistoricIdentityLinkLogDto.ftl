<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/identity-links/get-identity-link-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "Id of the Historic identity link entry."
    />
    
    <@lib.property
        name = "time"
        type = "string"
        format = "date-time"
        desc = "The time when the identity link is logged. 
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "type"
        type = "string"
        desc = "The type of identity link (candidate/assignee/owner)."
    />
    
    <@lib.property
        name = "userId"
        type = "string"
        desc = "The id of the user/assignee."
    />
    
    <@lib.property
        name = "groupId"
        type = "string"
        desc = "The id of the group."
    />
    
    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task."
    />
    
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
        name = "operationType"
        type = "string"
        desc = "Type of operation (add/delete)."
    />
    
    <@lib.property
        name = "assignerId"
        type = "string"
        desc = "The id of the assigner."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant."
    />
    
    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the identity link should be removed by the History Cleanup job. 
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing this identity link."
        last = true
    />

</@lib.dto>
</#macro>