<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "rel"
        type = "string"
        desc = "The relation of the link to the object that belongs to." />

    <@lib.property
        name = "href"
        type = "string"
        desc = "The url of the link." />

    <@lib.property
        name = "method"
        type = "string"
        last = true
        desc = "The http method." />

</@lib.dto>
</#macro>