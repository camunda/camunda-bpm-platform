<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "LinkableDto" >

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the deployment." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the deployment." />

    <@lib.property
        name = "deploymentTime"
        type = "string"
        format = "date-time"
        desc = "The time when the deployment was created." />

    <@lib.property
        name = "source"
        type = "string"
        desc = "The source of the deployment." />

    <@lib.property
        name = "name"
        type = "string"
        last = true
        desc = "The name of the deployment." />

</@lib.dto>
</#macro>