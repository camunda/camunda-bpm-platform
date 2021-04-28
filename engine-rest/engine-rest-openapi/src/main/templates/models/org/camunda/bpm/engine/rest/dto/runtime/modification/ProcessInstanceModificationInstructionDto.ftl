<#macro dto_macro docsUrl="">
<@lib.dto
    required = [ "type" ] >

    <#-- NOTE: Please consider adjusting the RestartProcessInstanceModificationInstructionDto
         if the properties are valid there as well.
         The DTO was created separately as it does not contain
         all of these properties and the description differs too much.
         Also check the MultipleProcessInstanceModifcationInstructionDto if you need changes for the Modification endpoints.
         -->

    <@lib.property
        name = "type"
        type = "string"
        nullable = false
        enumValues = ['"cancel"', '"startBeforeActivity"', '"startAfterActivity"', '"startTransition"']
        desc = "**Mandatory**. One of the following values: `cancel`, `startBeforeActivity`, `startAfterActivity`, `startTransition`.

                * A cancel instruction requests cancellation of a single activity instance or all instances of one activity.
                * A startBeforeActivity instruction requests to enter a given activity.
                * A startAfterActivity instruction requests to execute the single outgoing sequence flow of a given activity.
                * A startTransition instruction requests to execute a specific sequence flow." />

    <@lib.property
        name = "variables"
        type = "ref"
        dto = "TriggerVariableValueDto"
        desc = "Can be used with instructions of type `startBeforeActivity`, `startAfterActivity`, and `startTransition`.
                A JSON object containing variable key-value pairs. Each key is a variable name and each value a JSON variable value object." />

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "Can be used with instructions of types `startTransition`. Specifies the sequence flow to start." />

    <@lib.property
        name = "transitionId"
        type = "string"
        desc = "Can be used with instructions of types `startTransition`. Specifies the sequence flow to start." />

    <@lib.property
        name = "activityInstanceId"
        type = "string"
        desc = "Can be used with instructions of type `cancel`. Specifies the activity instance to cancel.
                Valid values are the activity instance IDs supplied by the [Get Activity Instance request](${docsUrl}/reference/rest/process-instance/get-activity-instances/)." />

    <@lib.property
        name = "transitionInstanceId"
        type = "string"
        desc = "Can be used with instructions of type `cancel`. Specifies the transition instance to cancel.
                Valid values are the transition instance IDs supplied by the [Get Activity Instance request](${docsUrl}/reference/rest/process-instance/get-activity-instances/)." />

    <@lib.property
        name = "ancestorActivityInstanceId"
        type = "string"
        desc = "Can be used with instructions of type `startBeforeActivity`, `startAfterActivity`, and `startTransition`.
                Valid values are the activity instance IDs supplied by the Get Activity Instance request.
                If there are multiple parent activity instances of the targeted activity,
                this specifies the ancestor scope in which hierarchy the activity/transition is to be instantiated.

                Example: When there are two instances of a subprocess and an activity contained in the subprocess is to be started,
                this parameter allows to specifiy under which subprocess instance the activity should be started." />

    <@lib.property
        name = "cancelCurrentActiveActivityInstances"
        type = "boolean"
        last = true
        desc = "Can be used with instructions of type cancel. Prevents the deletion of new created activity instances." />

</@lib.dto>

</#macro>