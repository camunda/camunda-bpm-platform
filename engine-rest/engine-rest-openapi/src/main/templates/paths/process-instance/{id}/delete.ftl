<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteProcessInstance"
      tag = "Process Instance"
      summary = "Delete"
      desc = "Deletes a running process instance by id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process instance to be deleted."/>

    <@lib.parameter
        name = "skipCustomListeners"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        desc = "If set to true, the custom listeners will be skipped." />

    <@lib.parameter
        name = "skipIoMappings"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        desc = "If set to true, the input/output mappings will be skipped." />

    <@lib.parameter
        name = "skipSubprocesses"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        desc = "If set to true, subprocesses related to deleted processes will be skipped." />

    <@lib.parameter
        name = "failIfNotExists"
        location = "query"
        type = "boolean"
        defaultValue = "true"
        last = true
        desc = "If set to false, the request will still be successful if the process id is not found." />

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
                Process instance with given id does not exist. " />

  }
}
</#macro>