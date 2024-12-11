<#macro endpoint_macro docsUrl="">
  {

    <@lib.endpointInfo
        id = "updateProcessInstanceComment"
        tag = "Process Instance Comment"
        summary = "Update"
        desc = "Updates a Comment." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id associated of a process instance of a comment to be updated."/>

  ],

    <@lib.requestBody
        mediaType = "application/json"
        dto = "CommentDto"
        requestDesc = "**Note:** Only the `id` and `message` properties will be used. Every other
          property passed to this endpoint will be ignored."
        examples = ['"example-1": {
                             "summary": "PUT /process-instance/aProcessInstanceId/comment",
                             "value": {
                                "id": "75bc161a-12da-11e4-7d3a-f4ccdc10a445",
                                "message": "a process instance comment"
                             }
                           }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."  />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if a given process instance id or comment id is invalid or history is disabled in the engine.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

    <@lib.response
        code = "404"
        dto = "AuthorizationExceptionDto"
       last = true
        desc = "The authenticated user is unauthorized to update this resource. See the
                    [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                    for the error response format." />

   }
  }

</#macro>