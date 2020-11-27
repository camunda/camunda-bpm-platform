<@lib.dto>

    <@lib.property
        name = "placeholder"
        type = "string"
        desc = "A placeholder string that can be used to display an internationalized message to the user."
    />

    <@lib.property
        name = "parameter"
        type = "object"
        last = true
        addProperty = "\"additionalProperties\": { \"type\": \"string\"}"
        desc = "A map of parameters that can be used to display a parameterized message to the use"
    />

</@lib.dto>