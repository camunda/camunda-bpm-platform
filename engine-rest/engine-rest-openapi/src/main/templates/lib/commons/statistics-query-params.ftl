
    <@lib.parameter
        name = "failedJobs"
        location = "query"
        type = "boolean"
        desc = "Whether to include the number of failed jobs in the result or not. Valid values are `true` or `false`."/>

    <@lib.parameter
        name = "incidents"
        location = "query"
        type = "boolean"
        desc = "Valid values for this property are `true` or `false`.
                If this property has been set to `true` the result will include the corresponding number of incidents
                for each occurred incident type.
                If it is set to `false`, the incidents will not be included in the result.
                Cannot be used in combination with `incidentsForType`."/>

    <@lib.parameter
        name = "incidentsForType"
        location = "query"
        type = "string"
        last = last
        desc = "If this property has been set with any incident type (i.e., a string value) the result
                will only include the number of incidents for the assigned incident type.
                Cannot be used in combination with `incidents`.
                See the [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types)
                for a list of incident types."/>
