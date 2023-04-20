<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteJob"
      tag = "Job"
      summary = "Delete Job"
      desc = "Deletes a job by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job to be deleted."
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
        desc = "The job could not be deleted. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>