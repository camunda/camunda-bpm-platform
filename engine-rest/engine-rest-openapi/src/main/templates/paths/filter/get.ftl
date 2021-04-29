<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getFilterList"
      tag = "Filter"
      summary = "Get Filters"
      desc = "Queries for a list of filters using a list of parameters. The size of the result
              set can be retrieved
              by using the [Get Filter Count](${docsUrl}/reference/rest/filter/get-query-count/) method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/filter-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <@lib.parameter
            name = "itemCount"
            location = "query"
            type = "boolean"
            desc = "If set to `true`, each filter result will contain an `itemCount` property
               with the number of items matched by the filter itself." />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "FilterDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/filter?resourceType=Task`",
                       "value": [
                         {
                           "id": "aFilter",
                           "resourceType": "Task",
                           "name": "My Filter",
                           "owner": "jonny1",
                           "query": {
                             "assignee": "jonny1"
                           },
                           "properties": {
                             "color": "#58FA58",
                             "description": "Filters assigned to me"
                           }
                         },
                         {
                           "id": "anotherFilter",
                           "resourceType": "Task",
                           "name": "Accountants Filter",
                           "owner": "demo",
                           "query": {
                             "candidateGroup": "accountant"
                           },
                           "properties": {
                             "description": "Filters assigned to me",
                             "priority": 10
                           }
                         }
                       ]
                     }',
                     '"example-2": {
                       "summary": "request with itemCount",
                       "description": "GET `/filter?resourceType=Task&itemCount=true`",
                       "value": [
                         {
                           "id": "aFilter",
                           "resourceType": "Task",
                           "name": "My Filter",
                           "owner": "jonny1",
                           "query": {
                             "assignee": "jonny1"
                           },
                           "properties": {
                             "color": "#58FA58",
                             "description": "Filters assigned to me"
                           },
                           "itemCount": 13
                         },
                         {
                           "id": "anotherFilter",
                           "resourceType": "Task",
                           "name": "Accountants Filter",
                           "owner": "demo",
                           "query": {
                             "candidateGroup": "accountant"
                           },
                           "properties": {
                             "description": "Filters assigned to me",
                             "priority": 10
                           },
                           "itemCount": 42
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                Returned if some of the query parameters are invalid, for example if
                a `sortOrder`
                parameter is supplied, but no `sortBy` is specified. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>