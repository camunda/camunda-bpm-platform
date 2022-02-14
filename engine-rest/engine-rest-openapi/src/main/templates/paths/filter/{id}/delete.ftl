<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/delete/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteFilter"
      tag = "Filter"
      summary = "Delete Filter"
      desc = "Deletes a filter by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to be deleted."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "
                The authenticated user is unauthorized to delete this filter.
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