<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "installation"
        type = "string"
        desc = "An id that is stored as process engine configuration property."/>

    <@lib.property
        name = "product"
        type = "object"
        additionalProperties = true
        dto = "TelemetryProductDto"
        desc = "Information about the product collection telemetry data."
        last = true
        />

</@lib.dto>

</#macro>