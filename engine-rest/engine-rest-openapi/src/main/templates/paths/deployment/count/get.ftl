{

  <@lib.endpointInfo
      id = "getDeploymentsCount"
      tag = "Deployment"
      desc = "Queries for the number of deployments that fulfill given parameters. Takes the same parameters as the
              [Get Deployments](${docsUrl}/reference/rest/deployment/get-query/) method." />

  "parameters" : [

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
        type = "string"
        desc = "Filter by the deployment source whereby source is equal to `null`."/>

    <@lib.parameter
        name = "tenantIdIn"
        location = "query"
        type = "string"
        desc = "Filter by a comma-separated list of tenant ids. A deployment must have one of the given tenant ids."/>

    <@lib.parameter
        name = "withoutTenantId"
        location = "query"
        type = "string"
        desc = "Only include deployments which belong to no tenant. Value may only be `true`, as `false` is the default
                behavior."/>

    <@lib.parameter
        name = "includeDeploymentsWithoutTenantId"
        location = "query"
        type = "string"
        desc = "Include deployments which belong to no tenant. Can be used in combination with `tenantIdIn`. Value may
                only be `true`, as `false` is the default behavior."/>

    <@lib.parameter
        name = "after"
        location = "query"
        type = "string"
        desc = "Restricts to all deployments after the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."/>

    <@lib.parameter
        name = "before"
        location = "query"
        type = "string"
        last = true
        desc = "Restricts to all deployments before the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."/>

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/deployment/count?name=deploymentName`",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example, if an invalid operator for variable
                comparison is used. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
