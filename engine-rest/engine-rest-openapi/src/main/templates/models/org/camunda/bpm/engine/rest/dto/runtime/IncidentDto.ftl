<#macro dto_macro docsUrl="">
<@lib.dto>
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the incident." />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition this incident is associated with." />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance this incident is associated with." />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The id of the execution this incident is associated with." />

    <@lib.property
        name = "incidentTimestamp"
        type = "string"
        format = "date-time"
        desc = "The time this incident happened. By [default](${docsUrl}/reference/rest/overview/date-format/),
                the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`." />

    <@lib.property
        name = "incidentType"
        type = "string"
        desc = "The type of incident, for example: `failedJobs` will be returned in case of an incident which identified
                a failed job during the execution of a process instance. See the
                [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types) for a list of incident types." />

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity this incident is associated with." />

    <@lib.property
        name = "failedActivityId"
        type = "string"
        desc = "The id of the activity on which the last exception occurred." />

    <@lib.property
        name = "causeIncidentId"
        type = "string"
        desc = "The id of the associated cause incident which has been triggered." />

    <@lib.property
        name = "rootCauseIncidentId"
        type = "string"
        desc = "The id of the associated root cause incident which has been triggered." />

    <@lib.property
        name = "configuration"
        type = "string"
        desc = "The payload of this incident." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant this incident is associated with." />

    <@lib.property
        name = "incidentMessage"
        type = "string"
        desc = "The message of this incident." />

    <@lib.property
        name = "jobDefinitionId"
        type = "string"
        desc = "The job definition id the incident is associated with." />
        
    <@lib.property
        name = "annotation"
        type = "string"
        last = true
        desc = "The annotation set to the incident." />
</@lib.dto>

</#macro>