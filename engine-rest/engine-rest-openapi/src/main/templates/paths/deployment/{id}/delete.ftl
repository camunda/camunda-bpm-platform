<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteDeployment"
      tag = "Deployment"
      summary = "Delete"
      desc = "Deletes a deployment by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the deployment to be deleted." />

    <@lib.parameter
        name = "cascade"
        location = "query"
        type = "boolean"
        defaultValue = 'false'
        desc = "`true`, if all process instances, historic process instances and jobs for this deployment
                should be deleted." />

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
        desc = "`true`, if all input/output mappings should not be invoked." />

  ],

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "A Deployment with the provided id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>