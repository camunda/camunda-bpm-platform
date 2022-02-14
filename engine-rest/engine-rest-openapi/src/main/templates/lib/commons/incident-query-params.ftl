    <@lib.parameter
        name = "incidentId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that have the given id." />

    <@lib.parameter
        name = "incidentType"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that belong to the given incident type. See the
                [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types) for a list of incident
                types." />

    <@lib.parameter
        name = "incidentMessage"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that have the given incident message." />

    <@lib.parameter
        name = "incidentMessageLike"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that incidents message is a substring of the given value. The string can include
                the wildcard character '%' to express like-strategy: starts with (`string%`), ends with (`%string`) or
                contains (`%string%`)." />

    <@lib.parameter
        name = "processDefinitionId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that belong to a process definition with the given id." />

    <@lib.parameter
        name = "processDefinitionKeyIn"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that belong to a process definition with the given keys. Must be a
        comma-separated list." />

    <@lib.parameter
        name = "processInstanceId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that belong to a process instance with the given id." />

    <@lib.parameter
        name = "executionId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that belong to an execution with the given id." />

    <@lib.parameter
        name = "incidentTimestampBefore"
        location = "query"
        type = "string"
        format = "date-time"
        desc = "Restricts to incidents that have an incidentTimestamp date before the given date. 
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date
                must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

    <@lib.parameter
        name = "incidentTimestampAfter"
        location = "query"
        type = "string"
        format = "date-time"
        desc = "Restricts to incidents that have an incidentTimestamp date after the given date. 
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date
                must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

    <@lib.parameter
        name = "activityId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that belong to an activity with the given id." />

    <@lib.parameter
        name = "failedActivityId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that were created due to the failure of an activity with the given id." />

    <@lib.parameter
        name = "causeIncidentId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that have the given incident id as cause incident." />

    <@lib.parameter
        name = "rootCauseIncidentId"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that have the given incident id as root cause incident." />

    <@lib.parameter
        name = "configuration"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that have the given parameter set as configuration." />

    <@lib.parameter
        name = "tenantIdIn"
        location = "query"
        type = "string"
        desc = "Restricts to incidents that have one of the given comma-separated tenant ids." />

    <@lib.parameter
        name = "jobDefinitionIdIn"
        location = "query"
        type = "string"
        last = last
        desc = "Restricts to incidents that have one of the given comma-separated job definition ids." />
