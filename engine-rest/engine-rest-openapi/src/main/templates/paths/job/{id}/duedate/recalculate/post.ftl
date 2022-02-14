<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/post-recalculate-job-duedate/index.html -->
{
  <@lib.endpointInfo
      id = "recalculateDuedate"
      tag = "Job"
      summary = "Recalculate Job Due Date"
      desc = "Recalculates the due date of a job by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job to be updated."
      />

      <@lib.parameter
          name = "creationDateBased"
          location = "query"
          type = "boolean"
          desc = "Recalculate the due date based on the creation date of the job or the current date.
                  Value may only be `false`, as `true` is the default behavior. "
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
        desc = "Job with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The due date could not be recalculated successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>