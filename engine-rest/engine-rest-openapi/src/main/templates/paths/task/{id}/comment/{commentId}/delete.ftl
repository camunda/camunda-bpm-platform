<#macro endpoint_macro docsUrl="">
  {

    <@lib.endpointInfo
        id = "deleteTaskComment"
        tag = "Task Comment"
        summary = "Delete"
        desc = "Removes a comment from a task by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task." />

    <@lib.parameter
        name = "commentId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the comment to be removed." />

  ],

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if a given task id or comment id is invalid.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

    <@lib.response
        code = "401"
        dto = "ExceptionDto"
        desc = "The authenticated user is unauthorized to delete this resource. See the
                    [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                    for the error response format."/>

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "The history of the engine is disabled. See the [Introduction](/reference/rest/overview/#error-handling)
                    for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The comment of a task could not be deleted successfully.
                    See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                    for the error response format." />

    }
  }

</#macro>