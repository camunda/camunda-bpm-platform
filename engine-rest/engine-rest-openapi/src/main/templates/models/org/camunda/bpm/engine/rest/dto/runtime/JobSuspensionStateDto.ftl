<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/put-activate-suspend-by-proc-def-key/index.html -->
<#-- Note: This dto describes functionality which is described in 4 different doc pages under put-activate-suspend-by-*
If you come here from PUT /job/{id}/suspended please note that this enpoint uses SuspensionStateDto because it only uses
the suspended attribute. Should you need to extend it, it is probably a good idea to create an additional file. -->

<@lib.dto extends="SuspensionStateDto"
          desc = "Defines by which selection criterion to activate or suspend jobs.
                  This selection criterion are mutually exclusive and can only be on of:
                  * `jobDefinitionId`
                  * `processDefinitionId`
                  * `processInstanceId`
                  * `processDefinitionKey`">

    <@lib.property
        name = "jobDefinitionId"
        type = "string"
        desc = "The job definition id of the jobs to activate or suspend."
    />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The process definition id of the jobs to activate or suspend."
    />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The process instance id of the jobs to activate or suspend."
    />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The process definition key of the jobs to activate or suspend."
    />
    
    <@lib.property
        name = "processDefinitionTenantId"
        type = "string"
        desc = "Only activate or suspend jobs of a process definition which belongs to a tenant
                with the given id. Works only when selecting with `processDefinitionKey`."
    />

    <@lib.property
        name = "processDefinitionWithoutTenantId"
        type = "boolean"
        desc = "Only activate or suspend jobs of a process definition which belongs to no tenant.
                Value may only be `true`, as `false` is the default behavior. Works only when selecting with `processDefinitionKey`."
        last = true
    />


</@lib.dto>
</#macro>
