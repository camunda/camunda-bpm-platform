{
  "type": "object",
  "description": "A JSON object corresponding to the Activity Instance tree of the given process instance.",
  "properties": {
    <@lib.property
        name = "id"
        type = "string"
        description = "The id of the activity instance."/>

    <@lib.property
        name = "parentActivityInstanceId"
        type = "string"
        description = "The id of the parent activity instance, for example a sub process instance."/>

    <@lib.property
        name = "activityId"
        type = "string"
        description = "The id of the activity."/>

    <@lib.property
        name = "activityName"
        type = "string"
        description = "The name of the activity"/>

    <@lib.property
        name = "activityType"
        type = "string"
        description = "The type of activity (corresponds to the XML element name in the BPMN 2.0, e.g., 'userTask')"/>

    <@lib.property
        name = "processInstanceId"
        type = "string"
        description = "The id of the process instance this activity instance is part of."/>

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        description = "The id of the process definition."/>

    <@lib.property
        name = "childActivityInstances"
        type = "array"
        dto = "ActivityInstanceDto"
        description = "A list of child activity instances."/>

    <@lib.property
        name = "childTransitionInstances"
        type = "array"
        dto="TransitionInstanceDto"
        description = "A list of child transition instances. A transition instance represents an execution waiting in an asynchronous continuation."/>

    <@lib.property
        name = "executionIds"
        type = "array"
        itemType = "string"
        description = "A list of execution ids."/>

    <@lib.property
        name = "incidentIds"
        type = "array"
        itemType = "string"
        description = "A list of incident ids."/>

    <@lib.property
        name = "incidents"
        type = "array"
        dto="ActivityInstanceIncidentDto"
        last = true
        description = "A list of JSON objects containing incident specific properties:
* id: the id of the incident
* activityId: the activity id in which the incident occurred"/>
  }
}
