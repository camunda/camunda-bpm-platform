<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "A JSON object corresponding to the Activity Instance tree of the given process instance." >

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the activity instance."/>

    <@lib.property
        name = "parentActivityInstanceId"
        type = "string"
        desc = "The id of the parent activity instance, for example a sub process instance."/>

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity."/>

    <@lib.property
        name = "activityName"
        type = "string"
        desc = "The name of the activity"/>

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the activity. This property is deprecated. Please use 'activityName'."/>

    <@lib.property
        name = "activityType"
        type = "string"
        desc = "The type of activity (corresponds to the XML element name in the BPMN 2.0, e.g., 'userTask')"/>

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance this activity instance is part of."/>

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition."/>

    <@lib.property
        name = "childActivityInstances"
        type = "array"
        dto = "ActivityInstanceDto"
        desc = "A list of child activity instances."/>

    <@lib.property
        name = "childTransitionInstances"
        type = "array"
        dto="TransitionInstanceDto"
        desc = "A list of child transition instances.
                A transition instance represents an execution waiting in an asynchronous continuation."/>

    <@lib.property
        name = "executionIds"
        type = "array"
        itemType = "string"
        desc = "A list of execution ids."/>

    <@lib.property
        name = "incidentIds"
        type = "array"
        itemType = "string"
        desc = "A list of incident ids."/>

    <@lib.property
        name = "incidents"
        type = "array"
        dto="ActivityInstanceIncidentDto"
        last = true
        desc = "A list of JSON objects containing incident specific properties:
                * `id`: the id of the incident
                * `activityId`: the activity id in which the incident occurred"/>

</@lib.dto>

</#macro>
