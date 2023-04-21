    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the ${instanceType} the results are aggregated for."/>

    <@lib.property
        name = "instances"
        type = "integer"
        format = "int32"
        nullable = false
        desc = "The total number of running process instances of this ${instanceType}."/>

    <@lib.property
        name = "failedJobs"
        type = "integer"
        format = "int32"
        nullable = false
        desc = "The total number of failed jobs for the running instances.
                **Note**: Will be `0` (not `null`), if failed jobs were excluded."/>

    <@lib.property
        name = "incidents"
        type = "array"
        dto = "IncidentStatisticsResultDto"
        desc = "Each item in the resulting array is an object which contains `incidentType` and `incidentCount`.
                **Note**: Will be an empty array, if `incidents` or `incidentsForType` were excluded.
                Furthermore, the array will be also empty if no incidents were found."/>

    <@lib.property
        name = "@class"
        type = "string"
        desc = "The fully qualified class name of the data transfer object class.
                The class name might change in future releases."
        last = last />
