<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getQueryGroups"
      tag = "Group"
      summary = "Get List"
      desc = "Queries for a list of groups using a list of parameters. The size of the result set can be retrieved
              by using the [Get Group Count](${docsUrl}/reference/rest/group/get-query-count) method." />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/group-query-params.ftl" >

    <#assign last = false >

    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >

    <@lib.parameters
        object = params
        last = true
    />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "GroupDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/group?name=Sales`",
                       "value": [
                         {"id":"sales",
                          "name":"Sales",
                          "type":"Organizational Unit"}
                       ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy` is specified. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>