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
        desc = "The id of the process instance." />

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
        desc = "Returned if a given process instance id or comment id is invalid or history is disabled in the engine.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

    <@lib.response
        code = "404"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "The authenticated user is unauthorized to delete this resource. See the
                        [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                        for the error response format." />

    }
  }

</#macro>