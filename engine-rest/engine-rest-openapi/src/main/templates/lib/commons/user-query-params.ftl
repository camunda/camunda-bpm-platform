<@lib.parameter
    name = "id"
    location = "query"
    type = "string"
    desc = "Filter by user id"/>

<@lib.parameter
    name = "idIn"
    location = "query"
    type = "string"
    desc = "Filter by a comma-separated list of user ids." />

<@lib.parameter
    name = "firstName"
    location = "query"
    type = "string"
    desc = "Filter by the first name of the user. Exact match."/>

<@lib.parameter
    name = "firstNameLike"
    location = "query"
    type = "string"
    desc = "Filter by the first name that the parameter is a substring of."/>

<@lib.parameter
    name = "lastName"
    location = "query"
    type = "string"
    desc = "Filter by the last name of the user. Exact match."/>

<@lib.parameter
    name = "lastNameLike"
    location = "query"
    type = "string"
    desc = "Filter by the last name that the parameter is a substring of."/>

<@lib.parameter
    name = "email"
    location = "query"
    type = "string"
    desc = "Filter by the email of the user. Exact match."/>

<@lib.parameter
    name = "emailLike"
    location = "query"
    type = "string"
    desc = "Filter by the email that the parameter is a substring of."/>

<@lib.parameter
    name = "memberOfGroup"
    location = "query"
    type = "string"
    desc = "Filter for users which are members of the given group."/>

<@lib.parameter
    name = "memberOfTenant"
    location = "query"
    type = "string"
    desc = "Filter for users which are members of the given tenant."/>

<@lib.parameter
    name = "potentialStarter"
    location = "query"
    type = "string"
    last = last
    desc = "Only select Users that are potential starter for the given process definition."/>