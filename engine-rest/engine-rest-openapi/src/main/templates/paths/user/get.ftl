<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getUsers"
      tag = "User"
      summary = "Get List"
      desc = "Query for a list of users using a list of parameters.
              The size of the result set can be retrieved by using the Get User Count method.
              [Get User Count](${docsUrl}/reference/rest/user/get-query-count/) method." />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/user-query-params.ftl" >

    <#assign sortByValues = [ '"userId"', '"firstName"', '"lastName"', '"email"' ] >
    <#include "/lib/commons/sort-params.ftl" >

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "UserProfileDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/user?firstName=John`",
                       "value": [
                         {
                           "id": "jonny1",
                           "firstName": "John",
                           "lastName": "Doe",
                           "email": "anEmailAddress"
                         },
                         {
                            "id": "jonny2",
                            "firstName": "John",
                            "lastName": "Smoe",
                            "email": "anotherEmailAddress"
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

</#macro>