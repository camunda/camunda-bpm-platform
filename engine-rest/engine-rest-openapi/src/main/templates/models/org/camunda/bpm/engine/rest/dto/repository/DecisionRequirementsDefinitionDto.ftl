<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the decision requirements definition" />

    <@lib.property
        name = "key"
        type = "string"
        desc = "The key of the decision requirements definition, i.e., the id of the DMN 1.0 XML decision definition." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the decision requirements definition." />

    <@lib.property
        name = "category"
        type = "string"
        desc = "The category of the decision requirements definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        desc = "The version of the decision requirements definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        desc = "The file name of the decision requirements definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        desc = "The deployment id of the decision requirements definition." />

    <@lib.property
        name = "tenantId"
        type = "string"
        last = true
        desc = "The tenant id of the decisionrequirements definition." />

</@lib.dto>
</#macro>