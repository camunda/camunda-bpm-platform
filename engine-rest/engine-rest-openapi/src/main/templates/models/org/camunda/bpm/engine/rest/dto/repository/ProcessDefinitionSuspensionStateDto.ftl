<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A `Boolean` value which indicates whether to activate or suspend all process definitions with the given key.
                When the value is set to `true`, all process definitions with the given key will be suspended and
                when the value is set to `false`, all process definitions with the given key will be activated." />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definitions to activate or suspend." />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definitions to activate or suspend." />

    <@lib.property
        name = "includeProcessInstances"
        type = "boolean"
        desc = "A `Boolean` value which indicates whether to activate or suspend also all process instances of 
                the process definitions with the given key.
                When the value is set to `true`, all process instances of the process definitions with the given key
                will be activated or suspended and when the value is set to `false`, the suspension state of 
                all process instances of the process definitions with the given key will not be updated." />

    <@lib.property
        name = "executionDate"
        type = "string"
        format = "date-time"
        last = true
        desc = "The date on which all process definitions with the given key will be activated or suspended.
                If `null`, the suspension state of all process definitions with the given key is updated immediately.
                By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

</@lib.dto>
</#macro>