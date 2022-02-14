<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteProcessDefinitionsByKeyAndTenantId"
      tag = "Process Definition"
      summary = "Delete By Key"
      desc = "Deletes process definitions by a given key and which belong to a tenant id." />

  "parameters": [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the process definitions to be deleted."/>

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the tenant the process definitions belong to."/>

    <@lib.parameter
        name = "cascade"
        location = "query"
        type = "boolean"
        desc = "`true`, if all process instances, historic process instances and jobs
                for this process definition should be deleted."/>

    <@lib.parameter
        name = "skipCustomListeners"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        desc = "`true`, if only the built-in ExecutionListeners should be notified with the end event." />

    <@lib.parameter
        name = "skipIoMappings"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        last = true
        desc = "A boolean value to control whether input/output mappings should be executed during deletion.
                `true`, if input/output mappings should not be invoked." />

  ],
  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "Forbidden
                The process definitions with the given `key` cannot be deleted due to missing permissions.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Not found
                Process definition with given key does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
</#macro>