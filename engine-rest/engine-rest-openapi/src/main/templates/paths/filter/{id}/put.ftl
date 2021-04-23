<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/put-update/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "updateFilter"
      tag = "Filter"
      summary = "Update Filter"
      desc = "Updates an existing filter."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to be updated."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CreateFilterDto"
      examples = ['"example-1": {
                     "summary": "request",
                     "description": "PUT `/filter/aFilterID`",
                     "value": {
                       "resourceType": "Task",
                       "name": "My Tasks",
                       "owner": "jonny1",
                       "query": {
                         "assignee": "jonny1"
                       },
                       "properties": {
                         "color": "#99CCFF",
                         "description": "Tasks assigned to me",
                         "priority": -10
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                Filter was invalid. See
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "
                The authenticated user is unauthorized to update this filter.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "
                Filter cannot be found. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>