{
  "operationId": "deleteProcessInstance",
  "description": "Deletes a running process instance by id.",
  "tags": [
    "Process instance"
  ],
  "parameters": [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        description = "The id of the process instance to be deleted."/>


    <@lib.parameter
        name = "skipCustomListeners"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        description = "If set to true, the custom listeners will be skipped." />

    <@lib.parameter
        name = "skipIoMappings"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        description = "If set to true, the input/output mappings will be skipped." />

    <@lib.parameter
        name = "skipSubprocesses"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        description = "If set to true, subprocesses related to deleted processes will be skipped." />

    <@lib.parameter
        name = "failIfNotExists"
        location = "query"
        type = "boolean"
        defaultValue = "true"
        last = true
        description = "If set to false, the request will still be successful if the process id is not found." />

  ],
  "responses": {

    <@lib.response
        code = "204"
        description = "Request successful." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        description = "Not found\n\n
                Process instance with given id does not exist. " />

  }
}