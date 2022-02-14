<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteTaskVariable"
      tag = "Task Variable"
      summary = "Delete Task Variable"
      desc = "Removes a variable that is visible to a task. A variable is visible to a task if it is a local task
              variable or declared in a parent scope of the task. See documentation on
              [visiblity of variables](${docsUrl}/user-guide/process-engine/variables/)." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task to delete the variable from."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to be removed."/>

  ],

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Task id is null or does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>