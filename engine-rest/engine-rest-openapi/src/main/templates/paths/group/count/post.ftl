<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/group/post-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "queryGroupCount"
      tag = "Group"
      summary = "Get List Count (POST)"
      desc = "Queries for groups using a list of parameters and retrieves the count."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "GroupQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/group/count`",
                     "value": {
                       "name": "Sales"
                     }
                   }']
  />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                          "summary": "Status 200.",
                          "description": "POST `/group/count`",
                          "value": {
                            "count": 1
                          }
                        }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response
                format."
        last = true
    />

  }

}

</#macro>