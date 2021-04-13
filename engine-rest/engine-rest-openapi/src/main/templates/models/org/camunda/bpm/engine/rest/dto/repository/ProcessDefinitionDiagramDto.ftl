<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the process definition." />

    <@lib.property
        name = "bpmn20Xml"
        type = "string"
        last = true
        desc = "An escaped XML string containing the XML that this definition was deployed with.
                Carriage returns, line feeds and quotation marks are escaped." />

</@lib.dto>
</#macro>