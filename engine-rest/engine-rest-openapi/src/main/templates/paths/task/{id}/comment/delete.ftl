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
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if a given task id is invalid. Orhe history of the engine is disabled.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

    <@lib.response
        code = "404"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "The authenticated user is unauthorized to delete this resource. See the
                        [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                        for the error response format."
    />

    }
  }
</#macro>