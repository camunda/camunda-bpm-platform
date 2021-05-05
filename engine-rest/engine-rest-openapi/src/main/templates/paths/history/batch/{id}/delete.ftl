<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/delete/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteHistoricBatch"
      tag = "Historic Batch"
      summary = "Delete Historic Batch"
      desc = "Deletes a historic batch by id, including related historic job logs."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the batch to be deleted."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Historic batch with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
