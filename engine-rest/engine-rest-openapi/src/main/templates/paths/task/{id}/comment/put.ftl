<#macro endpoint_macro docsUrl="">
  {

    <@lib.endpointInfo
        id = "updateTaskComment"
        tag = "Task Comment"
        summary = "Update"
        desc = "Updates a Comment." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id associated of a task of a comment to be updated."/>

  ],

    <@lib.requestBody
        mediaType = "application/json"
        dto = "CommentDto"
        requestDesc = "**Note:** Only the `id` and `message` properties will be used. Every other
          property passed to this endpoint will be ignored."
        examples = ['"example-1": {
                             "summary": "PUT /task/aTaskId/comment",
                             "value": {
                                "id": "86cd272a-23ea-22e5-8e4a-e5bded20a556",
                                "message": "a task comment"
                             }
                           }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."  />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if a given task id or comment id is invalid. Or the history of the engine is disabled.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

    <@lib.response
        code = "404"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "The authenticated user is unauthorized to update this resource. See the
                        [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                        for the error response format."/>

   }
  }

</#macro>