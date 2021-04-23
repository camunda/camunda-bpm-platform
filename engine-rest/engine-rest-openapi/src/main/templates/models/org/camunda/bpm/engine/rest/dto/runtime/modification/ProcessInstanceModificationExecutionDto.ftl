<#macro dto_macro docsUrl="">
<@lib.dto
    required = [ "type" ] >

    <#-- NOTE: This DTO is used for the Modification Endpoints. For other endpoints that require this DTO, check the other files.
         The DTOs are duplicated because there are significant differences in the wording and properties.-->

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
        name = "activityId"
        type = "string"
        desc = "Can be used with instructions of types `startTransition`. Specifies the sequence flow to start." />

    <@lib.property
        name = "transitionId"
        type = "string"
        desc = "Can be used with instructions of types `startTransition`. Specifies the sequence flow to start." />


    <@lib.property
        name = "cancelCurrentActiveActivityInstances"
        type = "boolean"
        last = true
        desc = "Can be used with instructions of type cancel. Prevents the deletion of new created activity instances." />


</@lib.dto>

</#macro>
