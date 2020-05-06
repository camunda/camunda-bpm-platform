<@lib.dto>

    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        nullable = false
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
        nullable = false
        last = true
        desc = "The value of the metric aggregated by the interval."/>

</@lib.dto>
