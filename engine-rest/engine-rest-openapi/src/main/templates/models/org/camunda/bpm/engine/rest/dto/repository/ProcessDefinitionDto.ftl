<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the process definition" />

    <@lib.property
        name = "key"
        type = "string"
        desc = "The key of the process definition, i.e., the id of the BPMN 2.0 XML process definition." />

    <@lib.property
        name = "category"
        type = "string"
        desc = "The category of the process definition." />

    <@lib.property
        name = "description"
        type = "string"
        desc = "The description of the process definition." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the process definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        desc = "The version of the process definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        desc = "The file name of the process definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        desc = "The deployment id of the process definition." />

    <@lib.property
        name = "diagram"
        type = "string"
        desc = "The file name of the process definition diagram, if it exists." />

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A flag indicating whether the definition is suspended or not." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the process definition." />

    <@lib.property
        name = "versionTag"
        type = "string"
        desc = "The version tag of the process definition." />

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        desc = "History time to live value of the process definition.
                Is used within [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)." />

    <@lib.property
        name = "startableInTasklist"
        type = "boolean"
        last = true
        desc = "A flag indicating whether the process definition is startable in Tasklist or not." />

</@lib.dto>
</#macro>