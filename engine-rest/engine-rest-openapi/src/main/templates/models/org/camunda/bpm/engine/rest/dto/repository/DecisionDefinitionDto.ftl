<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the decision definition" />

    <@lib.property
        name = "key"
        type = "string"
        desc = "The key of the decision definition, i.e., the id of the DMN 1.0 XML decision definition." />

    <@lib.property
        name = "category"
        type = "string"
        desc = "The category of the decision definition." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the decision definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        desc = "The version of the decision definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        desc = "The file name of the decision definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        desc = "The deployment id of the decision definition." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the decision definition." />

    <@lib.property
        name = "decisionRequirementsDefinitionId"
        type = "string"
        desc = "The id of the decision requirements definition this decision definition belongs to." />

    <@lib.property
        name = "decisionRequirementsDefinitionKey"
        type = "string"
        desc = "The key of the decision requirements definition this decision definition belongs to." />

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        desc = "History time to live value of the decision definition.
                Is used within [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)." />

    <@lib.property
        name = "versionTag"
        type = "string"
        last = true
        desc = "The version tag of the decision definition." />

</@lib.dto>
</#macro>