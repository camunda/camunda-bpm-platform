<@lib.parameter
    name = "id"
    location = "query"
    type = "string"
    desc = "Filter by deployment id"/>

<@lib.parameter
    name = "name"
    location = "query"
    type = "string"
    desc = "Filter by the deployment name. Exact match."/>

<@lib.parameter
    name = "nameLike"
    location = "query"
    type = "string"
    desc = "Filter by the deployment name that the parameter is a substring of. The parameter can include the
            wildcard `%` to express like-strategy such as: starts with (`%`name), ends with (name`%`) or contains
            (`%`name`%`)." />

<@lib.parameter
    name = "source"
    location = "query"
    type = "string"
    desc = "Filter by the deployment source."/>

<@lib.parameter
    name = "withoutSource"
    location = "query"
    type = "boolean"
    defaultValue = 'false'
    desc = "Filter by the deployment source whereby source is equal to `null`."/>

<@lib.parameter
    name = "tenantIdIn"
    location = "query"
    type = "string"
    desc = "Filter by a comma-separated list of tenant ids. A deployment must have one of the given tenant ids."/>

<@lib.parameter
    name = "withoutTenantId"
    location = "query"
    type = "boolean"
    defaultValue = 'false'
    desc = "Only include deployments which belong to no tenant. Value may only be `true`, as `false` is the default
            behavior."/>

<@lib.parameter
    name = "includeDeploymentsWithoutTenantId"
    location = "query"
    type = "boolean"
    defaultValue = 'false'
    desc = "Include deployments which belong to no tenant. Can be used in combination with `tenantIdIn`. Value may
            only be `true`, as `false` is the default behavior."/>

<@lib.parameter
    name = "after"
    location = "query"
    type = "string"
    format = "date-time"
    desc = "Restricts to all deployments after the given date.
            By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
            `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."/>

<@lib.parameter
    name = "before"
    location = "query"
    type = "string"
    format = "date-time"
    last = last
    desc = "Restricts to all deployments before the given date.
            By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
            `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."/>