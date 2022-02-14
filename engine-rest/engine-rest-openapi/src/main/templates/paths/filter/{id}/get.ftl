<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getSingleFilter"
      tag = "Filter"
      summary = "Get Single Filter"
      desc = "Retrieves a single filter by id, according to the `Filter` interface in the engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to be retrieved."
      />

      <@lib.parameter
          name = "itemCount"
          location = "query"
          type = "boolean"
          desc = "If set to `true`, each filter result will contain an `itemCount`
                  property with the number of items matched by the filter itself."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "FilterDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/filter/aFilterId`",
                       "value": {
                         "id": "9917d731-3cde-11e4-b704-f0def1e59da8",
                         "name": "Accounting Tasks",
                         "owner": null,
                         "properties": {
                           "color": "#3e4d2f",
                           "description": "Tasks assigned to group accounting",
                           "priority": 5
                         },
                         "query": {
                           "candidateGroup": "accounting"
                         },
                         "resourceType": "Task"
                       }
                     }',
                     '"example-2": {
                       "summary": "request with itemCount",
                       "description": "GET `/filter/aFilterId?itemCount=true`",
                       "value": {
                         "id": "9917d731-3cde-11e4-b704-f0def1e59da8",
                         "name": "Accounting Tasks",
                         "owner": null,
                         "properties": {
                           "color": "#3e4d2f",
                           "description": "Tasks assigned to group accounting",
                           "priority": 5
                         },
                         "query": {
                           "candidateGroup": "accounting"
                         },
                         "resourceType": "Task",
                         "itemCount": 23
                       }
                     }']
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "The authenticated user is unauthorized to read this filter.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Filter with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>