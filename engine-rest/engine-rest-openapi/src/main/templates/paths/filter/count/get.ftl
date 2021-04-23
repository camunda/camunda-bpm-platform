<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getFilterCount"
      tag = "Filter"
      summary = "Get Filter Count"
      desc = "Retrieves the number of filters that fulfill a provided query. Corresponds to the
              size of the result set when using the 
              [Get Filters](${docsUrl}/reference/rest/filter/get-query/) method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/filter-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/filter/count?resourceType=Task&owner=aUserId`",
                       "value": {
                         "count": 3
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                Returned if some of the query parameters are invalid, for example if
                a `sortOrder`parameter is supplied, but no `sortBy`, or if an invalid operator
                for variable comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>