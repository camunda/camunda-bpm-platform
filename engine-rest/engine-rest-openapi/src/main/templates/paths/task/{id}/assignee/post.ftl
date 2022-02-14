<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "setAssignee"
      tag = "Task"
      summary = "Set Assignee"
      desc = "Changes the assignee of a task to a specific user.

              **Note:** The difference with the [Claim Task](${docsUrl}/reference/rest/task/post-claim/)
              method is that this method does not check if the task already has a user
              assigned to it." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to set the assignee for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "UserIdDto"
      requestDesc = "Provide the id of the user that will be the assignee of the task."
      examples = ['"example-1": {
                     "summary": "Request Body",
                     "description": "POST `/task/anId/assignee`",
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
        desc = "Task with given id does not exist or setting the assignee was not successful.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>