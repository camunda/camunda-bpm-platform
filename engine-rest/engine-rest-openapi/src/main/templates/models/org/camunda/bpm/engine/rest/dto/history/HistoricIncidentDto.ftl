<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the incident."
    />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition this incident is associated with."
    />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition this incident is associated with."
    />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The key of the process definition this incident is associated with."
    />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The id of the execution this incident is associated with."
    />

    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing this incident."
    />

    <@lib.property
        name = "createTime"
        type = "string"
        format = "date-time"
        desc = "The time this incident happened. 
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />

    <@lib.property
        name = "endTime"
        type = "string"
        format = "date-time"
        desc = "The time this incident has been deleted or resolved. 
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />

    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the incident should be removed by the History Cleanup job.
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />

    <@lib.property
        name = "incidentType"
        type = "string"
        desc = "The type of incident, for example: `failedJobs` will be returned in case of an
                incident which identified a failed job during the execution of a
                process instance. See the [User Guide](/manual/develop/user-
                guide/process-engine/incidents/#incident-types) for a list of
                incident types."
    />

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity this incident is associated with."
    />

    <@lib.property
        name = "failedActivityId"
        type = "string"
        desc = "The id of the activity on which the last exception occurred."
    />

    <@lib.property
        name = "causeIncidentId"
        type = "string"
        desc = "The id of the associated cause incident which has been triggered."
    />

    <@lib.property
        name = "rootCauseIncidentId"
        type = "string"
        desc = "The id of the associated root cause incident which has been triggered."
    />

    <@lib.property
        name = "configuration"
        type = "string"
        desc = "The payload of this incident."
    />

    <@lib.property
        name = "historyConfiguration"
        type = "string"
        desc = "The payload of this incident at the time when it occurred."
    />

    <@lib.property
        name = "incidentMessage"
        type = "string"
        desc = "The message of this incident."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant this incident is associated with."
    />
    
    <@lib.property
        name = "jobDefinitionId"
        type = "string"
        desc = "The job definition id the incident is associated with."
    />
    
    <@lib.property
        name = "open"
        type = "boolean"
        desc = "If true, this incident is open."
    />
    
    <@lib.property
        name = "deleted"
        type = "boolean"
        desc = "If true, this incident has been deleted."
    />
    
    <@lib.property
        name = "resolved"
        type = "boolean"
        desc = "If true, this incident has been resolved."
    />

    <@lib.property
        name = "annotation"
        type = "string"
        desc = "The annotation set to the incident."
        last = true
    />

</@lib.dto>
</#macro>