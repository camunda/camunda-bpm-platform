<@lib.dto>
    <#-- In the java code this inherits from UserCredentialsDto, but that brings an unnecessary
    attribute authenticatedUserPassword -->

    <@lib.property
        name = "username"
        type = "string"
        desc = "The username of a user."
    />

    <@lib.property
        name = "password"
        type = "string"
        desc = "A password of a user."
        last = true
    />

</@lib.dto>