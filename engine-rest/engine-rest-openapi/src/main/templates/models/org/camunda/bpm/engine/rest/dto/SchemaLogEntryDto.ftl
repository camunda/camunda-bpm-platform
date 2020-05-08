<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the schema log entry." />

    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        nullable = false
        desc = "The date and time of the schema update." />

    <@lib.property
        name = "version"
        type = "string"
        last = true
        desc = "The version of the schema." />

</@lib.dto>
