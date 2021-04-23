<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/post-create/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "createFilter"
      tag = "Filter"
      summary = "Create Filter"
      desc = "Creates a new filter."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CreateFilterDto"
      examples = ['"example-1": {
                     "summary": "request",
                     "description": "POST `/filter/create`",
                     "value": {
                       "resourceType": "Task",
                       "name": "Accounting Tasks",
                       "owner": "jonny1",
                       "query": {
                         "candidateGroup": "accounting"
                       },
                       "properties": {
                         "color": "#3e4d2f",
                         "description": "Tasks assigned to group accounting",
                         "priority": 5
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "FilterDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "POST `/filter/create`",
                       "value": {
                         "id": "aFilterId",
                         "resourceType": "Task",
                         "name": "Accounting Tasks",
                         "owner": "jonny1",
                         "query": {
                           "candidateGroup": "accounting"
                         },
                         "properties": {
                           "color": "#3e4d2f",
                           "description": "Tasks assigned to group accounting",
                           "priority": 5
                         }
                       }
                     }']
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
                The authenticated user is unauthorized to create a new filter. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>