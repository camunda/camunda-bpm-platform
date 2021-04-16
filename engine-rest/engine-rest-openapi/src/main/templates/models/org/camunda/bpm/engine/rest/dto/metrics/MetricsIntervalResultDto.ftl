<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        desc = "The interval timestamp."/>

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the metric."/>

    <@lib.property
        name = "reporter"
        type = "string"
        desc = "The reporter of the metric. `null` if the metrics are aggregated by reporter."/>

    <@lib.property
        name = "value"
        type = "integer"
        format = "int64"
        last = true
        desc = "The value of the metric aggregated by the interval."/>

</@lib.dto>

</#macro>