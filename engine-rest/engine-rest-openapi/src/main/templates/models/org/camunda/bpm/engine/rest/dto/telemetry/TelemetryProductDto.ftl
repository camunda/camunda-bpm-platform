<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the product (i.e., Camunda BPM Runtime)."/>

    <@lib.property
        name = "version"
        type = "string"
        desc = "The version of the process engine (i.e., 7.X.Y)."/>

    <@lib.property
        name = "edition"
        type = "string"
        desc = "The edition of the product (i.e., either community or enterprise)."/>

    <@lib.property
        name = "internals"
        type = "ref"
        additionalProperties = false
        dto = "TelemetryInternalsDto"
        last = true
        desc = "Internal data and metrics collected by the product."/>

</@lib.dto>

</#macro>