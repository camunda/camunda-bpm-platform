<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "customer"
        type = "string"
        desc = "The name of the customer the license was issued for."/>

    <@lib.property
        name = "type"
        type = "string"
        desc = "The license type."/>

        <@lib.property
        name = "valid-until"
        type = "string"
        format = "date"
        desc = "The expiration date of the license."/>

        <@lib.property
        name = "unlimited"
        type = "boolean"
        desc = "Flag that indicates if the license is unlimited."/>

        <@lib.property
        name = "features"
        type = "object"
        addProperty = "\"additionalProperties\": { \"type\": \"string\"}"
        desc = "A map of features included in the license."/>

        <@lib.property
        name = "raw"
        type = "string"
        last = true
        desc = "The raw license information."/>

</@lib.dto>

</#macro>