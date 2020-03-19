{

  <@lib.endpointInfo
      id = "updateTask"
      tag = "Task"
      desc = "Updates a task." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to be updated."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TaskDto" />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."  />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if a not valid `delegationState` is supplied. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "If the corresponding task cannot be found." />

  }
}
