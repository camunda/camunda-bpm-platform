<#macro endpoint_macro docsUrl="">
  {
    <@lib.endpointInfo
        id = "deleteTaskComments"
        tag = "Task Comment"
        summary = "Delete Task Comments"
        desc = "Deletes all comments of a task by task id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task for which all comments are to be deleted."/>

  ],
  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "401"
        dto = "ExceptionDto"
        desc = "The authenticated user is unauthorized to delete this resource. See the
                        [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                        for the error response format."
    />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "The history of the engine is disabled. See the [Introduction](/reference/rest/overview/#error-handling)
                        for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Comments of a task could not be deleted successfully.
                                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                                for the error response format." />
    }
  }
</#macro>