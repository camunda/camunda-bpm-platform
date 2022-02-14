<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/delete-local-variable/index.html -->
{
  <@lib.endpointInfo
      id = "deleteLocalExecutionVariable"
      tag = "Execution"
      summary = "Delete Local Execution Variable"
      desc = "Deletes a variable in the context of a given execution by id. Deletion does not
              propagate upwards in the execution hierarchy."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to delete the variable from."
      />

      <@lib.parameter
          name = "varName"
          location = "path"
          type = "string"
          required = true
          desc = "The name of the variable to delete."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
        last = true
    />

  }

}
</#macro>