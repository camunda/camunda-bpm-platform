<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteTask"
      tag = "Task"
      summary = "Delete"
      desc = "Removes a task by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to be removed."/>

  ],

  "responses" : {

    <@lib.response
        code = "204"
        dto = "TaskDto"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Bad Request. The Task with the given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The Task with the given id cannot be deleted because it is part of a running process or case instance.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for
                the error response format." />

  }
}

</#macro>