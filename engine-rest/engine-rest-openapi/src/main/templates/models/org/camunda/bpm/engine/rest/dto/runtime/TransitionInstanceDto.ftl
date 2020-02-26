{
  "type": "object",
  "description": "A JSON object corresponding to the Activity Instance tree of the given process instance.",
  "properties": {
    <@lib.property
        name="id"
        type="string"
        description="The id of the transition instance."/>

    <@lib.property
        name="parentActivityInstanceId"
        type="string"
        description="The id of the parent activity instance, for example a sub process instance."/>

    <@lib.property
        name="activityId"
        type="string"
        description="The id of the activity that this instance enters (asyncBefore job) or leaves (asyncAfter job)"/>

    <@lib.property
        name="activityName"
        type="string"
        description="The name of the activity that this instance enters (asyncBefore job) or leaves (asyncAfter job)"/>

    <@lib.property
        name="activityType"
        type="string"
        description="The type of the activity that this instance enters (asyncBefore job) or leaves (asyncAfter job)"/>

    <@lib.property
        name="processInstanceId"
        type="string"
        description="The id of the process instance this instance is part of."/>

    <@lib.property
        name="processDefinitionId"
        type="string"
        description="The id of the process definition."/>

    <@lib.property
        name="executionId"
        type="string"
        description="The execution id."/>

    <@lib.property
        name="incidentIds"
        type="array"
        itemType="string"
        description="A list of incident ids."/>

    <@lib.property
        name="incidents"
        type="array"
        dto="ActivityInstanceIncidentDto"
        last =true
        description="A list of JSON objects containing incident specific properties:
* `id`: the id of the incident
* `activityId`: the activity id in which the incident occurred"/>

  }
}
