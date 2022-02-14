<#macro dto_macro docsUrl="">
<@lib.dto
    required = [ "type" ] >

    <@lib.property
        name = "type"
        type = "string"
        nullable = false
        enumValues = ['"startBeforeActivity"', '"startAfterActivity"', '"startTransition"']
        desc = "**Mandatory**. One of the following values: `startBeforeActivity`, `startAfterActivity`, `startTransition`.

                * A `startBeforeActivity` instruction requests to enter a given activity.
                * A `startAfterActivity` instruction requests to execute the single outgoing sequence flow of a given activity.
                * A `startTransition` instruction requests to execute a specific sequence flow." />

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "**Can be used with instructions of types** `startBeforeActivity`
                and `startAfterActivity`. Specifies the sequence flow to start." />

    <@lib.property
        name = "transitionId"
        type = "string"
        last = true
        desc = "**Can be used with instructions of types** `startTransition`.
                Specifies the sequence flow to start." />

</@lib.dto>
</#macro>