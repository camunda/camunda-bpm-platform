<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "name"
        type = "string"
        nullable = false
        desc = "The name of the signal to deliver.

                **Note**: This property is mandatory." />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "Optionally specifies a single execution which is notified by the signal.

                **Note**: If no execution id is defined the signal is broadcasted to all subscribed
                handlers. "/>

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A JSON object containing variable key-value pairs. Each key is a variable name and
                each value a JSON variable value object."/>

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "Specifies a tenant to deliver the signal. The signal can only be received on
                executions or process definitions which belongs to the given tenant.

                **Note**: Cannot be used in combination with executionId."/>

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        last = true
        desc = "If true the signal can only be received on executions or process definitions which
                belongs to no tenant. Value may not be false as this is the default behavior.

                **Note**: Cannot be used in combination with `executionId`."/>

</@lib.dto>

</#macro>