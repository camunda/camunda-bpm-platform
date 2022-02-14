<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "unclaim"
      tag = "Task"
      summary = "Unclaim"
      desc = "Resets a task's assignee. If successful, the task is not assigned to a user." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to unclaim."/>

  ],

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."
        examples = ['"example-1": {
                      "value": "POST `/task/anId/unclaim`"
                    }']/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The Task with the given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>