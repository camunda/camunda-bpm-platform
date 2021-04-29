<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-activate-suspend-by-id/index.html -->
<#macro dto_macro docsUrl="">

<#assign noteMutualExclusive = "Note that only one of `processDefinitionId`, `processDefinitionKey` or a job definition
                                id (as path parameter) is allowed for a request." >
<#assign noteProcessDefinitionKey = "Note that this parameter will only be considered 
                                     in combination with `processDefinitionKey`." >

<@lib.dto>
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The process definition id of the job definitions to activate or suspend.
        
                ${noteMutualExclusive}"
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The process definition key of the job definitions to activate or suspend.
        
                ${noteMutualExclusive}"
    />

    
    <@lib.property
        name = "processDefinitionTenantId"
        type = "string"
        desc = "Only activate or suspend job definitions of a process definition which belongs to a
                tenant with the given id.
                
                ${noteProcessDefinitionKey}"
    />

    
    <@lib.property
        name = "processDefinitionWithoutTenantId"
        type = "string"
        desc = "Only activate or suspend job definitions of a process definition which belongs to
                no tenant. Value may only be `true`, as `false` is the default
                behavior.
                
                ${noteProcessDefinitionKey}"
    />

    
    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A `Boolean` value which indicates whether to activate or suspend the referenced job
                definitions. When the value is set to `true`, the job
                definitions will be suspended and when the value is set to `false`,
                they will be activated."
    />

    
    <@lib.property
        name = "includeJobs"
        type = "boolean"
        desc = "A `Boolean` value which indicates whether to activate or suspend also all jobs of
                the referenced job definitions. When the value is set to `true`, all jobs
                of the provided job definitions will be activated or suspended and
                when the value is set to `false`, the suspension state of all jobs
                of the provided job definitions will not be updated."
    />

    
    <@lib.property
        name = "executionDate"
        type = "string"
        format = "date-time"
        desc = "The date on which the referenced job definitions will be activated or suspended. If null,
                the suspension state of the given job definitions is updated
                immediately. By default*, the date must have the format `yyyy-MM-
                dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
        last = true
    />


</@lib.dto>
</#macro>