{

  <@lib.endpointInfo
      id = "delegate"
      tag = "Task"
      desc = "Delegates a task to another user." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to delegate."/>

  ],

  <#assign action = "the task should be delegated to" >
  <@lib.requestBody
      mediaType = "application/json"
      dto = "UserIdDto"
      examples = ['"example-1": {
                     "summary": "Request Body",
                     "description": "POST `/task/anId/delegate`",
                     "value": {
                       "userId": "aUserId"
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "If the task does not exist or delegation was not successful. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
