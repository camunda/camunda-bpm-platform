<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "installation"
        type = "string"
        desc = "An id which is unique for each installation of Camunda. It is stored once per database so all
                engines connected to the same database will have the same installation ID.
                The ID is used to identify a single installation of Camunda Platform."/>

    <@lib.property
        name = "product"
        type = "ref"
        additionalProperties = false
        dto = "TelemetryProductDto"
        desc = "Information about the product collection telemetry data."
        last = true
        />

</@lib.dto>

</#macro>