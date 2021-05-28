<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "An activity instance, incident pair." >

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the incident."/>

    <@lib.property
        name="activityId"
        type = "string"
        last = true
        desc = "The activity id in which the incident happened."/>

</@lib.dto>

</#macro>