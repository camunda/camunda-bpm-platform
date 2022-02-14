<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "attachment-name"
        type = "string"
        desc = "The name of the attachment." />

    <@lib.property
        name = "attachment-description"
        type = "string"
        desc = "The description of the attachment." />

    <@lib.property
        name = "attachment-type"
        type = "string"
        desc = "The type of the attachment." />

    <@lib.property
        name = "url"
        type = "string"
        desc = "The url to the remote content of the attachment." />

    <@lib.property
        name = "content"
        type = "string"
        format = "binary"
        last = true
        desc = "The content of the attachment." />

</@lib.dto>
</#macro>