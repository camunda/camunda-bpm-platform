<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        nullable = false
        desc = "The id of the process definition." />

    <@lib.property
        name = "bpmn20Xml"
        type = "string"
        nullable = false
        last = true
        desc = "An escaped XML string containing the XML that this definition was deployed with.
                Carriage returns, line feeds and quotation marks are escaped." />

</@lib.dto>