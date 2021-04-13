<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteHistoricVariableInstancesOfHistoricProcessInstance"
      tag = "Historic Process Instance"
      summary = "Delete Variable Instances"
      desc = "Deletes all variables of a process instance from the history by id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process instance for which all historic variables are to be deleted."/>

  ],
  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Not found
                Historic process instance with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#parse-exceptions) for the error response format." />

  }
}
</#macro>