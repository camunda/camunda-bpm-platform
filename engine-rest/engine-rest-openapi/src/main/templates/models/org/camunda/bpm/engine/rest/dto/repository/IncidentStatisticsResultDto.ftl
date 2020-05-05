<@lib.dto>

    <@lib.property
        name = "incidentType"
        type = "string"
        desc = "The type of the incident the number of incidents is aggregated for. See the [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types) for a list of incident types."/>

    <@lib.property
        name = "incidentCount"
        type = "integer"
        format = "int32"
        nullable = false
        last = true
        desc = "The total number of incidents for the corresponding incident type."/>

</@lib.dto>