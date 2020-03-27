{

  <@lib.endpointInfo
      id = "getDeployments"
      tag = "Deployment"
      desc = "Queries for deployments that fulfill given parameters. Parameters may be the properties of deployments,
              such as the id or name or a range of the deployment time. The size of the result set can be retrieved by
              using the [Get Deployment count](${docsUrl}/reference/rest/deployment/get-query-count/) method." />

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
        desc = "Restricts to all deployments before the given date.
                By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."/>

    <#assign last = false >
    <#assign sortBy = [ '"id"', '"name"', '"deploymentTime"', '"tenantId"' ] >
    <#include "/lib/commons/sort-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "DeploymentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/deployment?name=deploymentName`",
                       "value": [
                         {
                           "id": "someId",
                           "name": "deploymentName",
                           "source": "process application",
                           "tenantId": null,
                           "deploymentTime": "2013-04-23T13:42:43.000+0200"
                         }
                       ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy`, or if an invalid operator for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
