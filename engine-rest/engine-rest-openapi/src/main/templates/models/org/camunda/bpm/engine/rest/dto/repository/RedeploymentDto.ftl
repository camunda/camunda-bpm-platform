<#macro dto_macro docsUrl="">
<@lib.dto
    title = "RedeploymentDto"
    desc = "A JSON object with the following properties:" >

    <@lib.property
        name = "resourceIds"
        type = "array"
        itemType = "string"
        desc = "A list of deployment resource ids to re-deploy." />

    <@lib.property
        name = "resourceNames"
        type = "array"
        itemType = "string"
        desc = "A list of deployment resource names to re-deploy." />

    <@lib.property
        name = "source"
        type = "string"
        last = true
        desc = "Sets the source of the deployment." />

</@lib.dto>

</#macro>