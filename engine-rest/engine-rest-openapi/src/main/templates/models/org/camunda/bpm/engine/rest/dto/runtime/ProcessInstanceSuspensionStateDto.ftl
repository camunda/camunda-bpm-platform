<#macro dto_macro docsUrl="">
<@lib.dto extends = "SuspensionStateDto">

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The process definition id of the process instances to activate or suspend.

                **Note**: This parameter can be used only with combination of `suspended`." />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The process definition key of the process instances to activate or suspend.

                **Note**: This parameter can be used only with combination of `suspended`, `processDefinitionTenantId`, and `processDefinitionWithoutTenantId`." />

    <@lib.property
        name = "processDefinitionTenantId"
        type = "string"
        desc = "Only activate or suspend process instances of a process definition which belongs to a tenant with the given id.

                **Note**: This parameter can be used only with combination of `suspended`, `processDefinitionKey`, and `processDefinitionWithoutTenantId`." />

    <@lib.property
        name = "processDefinitionWithoutTenantId"
        type = "boolean"
        desc = "Only activate or suspend process instances of a process definition which belongs to no tenant.
                Value may only be true, as false is the default behavior.

                **Note**: This parameter can be used only with combination of `suspended`, `processDefinitionKey`, and `processDefinitionTenantId`." />

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list of process instance ids which defines a group of process instances
                which will be activated or suspended by the operation.

                **Note**: This parameter can be used only with combination of `suspended`, `processInstanceQuery`, and `historicProcessInstanceQuery`." />

    <@lib.property
        name = "processInstanceQuery"
        type = "ref"
        dto = "ProcessInstanceQueryDto" />

    <@lib.property
        name = "historicProcessInstanceQuery"
        type = "ref"
        last = true
        dto = "HistoricProcessInstanceQueryDto" />

</@lib.dto>
</#macro>