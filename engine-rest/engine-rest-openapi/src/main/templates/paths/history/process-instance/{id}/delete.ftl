<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteHistoricProcessInstance"
      tag = "Historic Process Instance"
      summary = "Delete"
      desc = "Deletes a process instance from the history by id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the historic process instance to be deleted."/>

    <@lib.parameter
        name = "failIfNotExists"
        location = "query"
        type = "boolean"
        last = true
        desc = "If set to `false`, the request will still be successful if the process id is not found." />

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
                Historic process instance with given id does not exist." />

  }
}
</#macro>