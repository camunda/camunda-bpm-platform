<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the user."
    />

    <@lib.property
        name = "firstName"
        type = "string"
        desc = "The firstname of the user."
    />

    <@lib.property
        name = "lastName"
        type = "string"
        desc = "The lastname of the user."
    />

    <@lib.property
        name = "displayName"
        type = "string"
        desc = "The displayName is the id, if firstName and lastName are null and firstName lastName otherwise."
        last = true
    />

</@lib.dto>