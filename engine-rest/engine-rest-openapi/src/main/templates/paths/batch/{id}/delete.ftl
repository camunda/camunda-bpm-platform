<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteBatch"
      tag = "Batch"
      summary = "Delete"
      desc = "Deletes a batch by id, including all related jobs and job definitions.
              Optionally also deletes the batch history." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the batch to be deleted." />

    <@lib.parameter
        name = "cascade"
        location = "query"
        type = "boolean"
        last = true
        desc = "`true`, if the historic batch and historic job logs for this batch should also be deleted."/>
  ],

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Batch with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}
</#macro>