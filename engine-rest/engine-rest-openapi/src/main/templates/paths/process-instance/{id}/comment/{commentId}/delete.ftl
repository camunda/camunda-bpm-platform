<#macro endpoint_macro docsUrl="">
  {

    <@lib.endpointInfo
        id = "deleteProcessInstanceComment"
        tag = "Process Instance Comment"
        summary = "Delete"
        desc = "Removes a comment from a process instance by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the processInstance." />

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
        code = "401"
        dto = "ExceptionDto"
        desc = "The authenticated user is unauthorized to delete this resource. See the
                        [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                        for the error response format." />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "The history of the engine is disabled. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                    for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The comment of a process instance could not be deleted successfully.
                    See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                    for the error response format." />

    }
  }

</#macro>