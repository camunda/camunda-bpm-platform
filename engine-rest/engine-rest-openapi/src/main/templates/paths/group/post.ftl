<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/group/post-query/index.html -->
{
  <@lib.endpointInfo
      id = "postQueryGroups"
      tag = "Group"
      summary = "Get List (POST)"
      desc = "Queries for a list of groups using a list of parameters.
              The size of the result set can be retrieved by using the
              [Get Group Count (POST)](${docsUrl}/reference/rest/group/post-query-count/) method."
  />

  "parameters" : [
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >
  ],

  <#assign requestMethod="POST"/>
  <@lib.requestBody
      mediaType = "application/json"
      dto = "GroupQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/group`",
                     "value": {
                       "name": "Sales"
                     }
                   }']
  />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "GroupDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                          "summary": "Status 200.",
                          "description": "POST `/group`",
                          "value": [
                            {
                              "id": "sales",
                              "name": "Sales",
                              "type": "Organizational Unit"
                            }
                          ]
                        }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder` parameter is supplied,
                but no `sortBy` is specified. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>