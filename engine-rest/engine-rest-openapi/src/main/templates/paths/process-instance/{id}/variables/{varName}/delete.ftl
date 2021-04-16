<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteProcessInstanceVariable"
      tag = "Process Instance"
      summary = "Delete Process Variable"
      desc = "Deletes a variable of a process instance by id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process instance to delete the variable from."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to delete."/>

  ],
  "responses": {

    <@lib.response
        code = "204"
        last = true
        desc = "Request successful."/>

  }
}
</#macro>