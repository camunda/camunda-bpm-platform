<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the process instance." />

    <@lib.property
        name = "definitionId"
        type = "string"
        desc = "The id of the process definition that this process instance belongs to." />

    <@lib.property
        name = "businessKey"
        type = "string"
        desc = "The business key of the process instance." />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The id of the case instance associated with the process instance." />

    <@lib.property
        name = "ended"
        type = "boolean"
        deprecated = true
        desc = "A flag indicating whether the process instance has ended or not. Deprecated: will always be false!" />

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A flag indicating whether the process instance is suspended or not." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the process instance." />

    <#-- ProcessInstanceDto extends the abstract LinkableDto -->
    <#assign last = true >
    <#assign resource = "process instance" >
    <#include "/lib/commons/linkable-dto-props.ftl" >

</@lib.dto>