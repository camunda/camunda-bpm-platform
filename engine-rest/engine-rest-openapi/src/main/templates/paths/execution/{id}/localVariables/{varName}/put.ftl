<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/put-local-variable/index.html -->
{
  <@lib.endpointInfo
      id = "putLocalExecutionVariable"
      tag = "Execution"
      summary = "Put Local Execution Variable"
      desc = "Sets a variable in the context of a given execution by id. Update does not
              propagate upwards in the execution hierarchy."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to set the variable for."
      />

      <@lib.parameter
          name = "varName"
          location = "path"
          type = "string"
          required = true
          desc = "The name of the variable to set."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "VariableValueDto"
      examples = ['"example-1": {
                     "summary": "PUT `/execution/anExecutionId/localVariables/aVarName`",
                     "value": {
                       "value": "someValue",
                       "type": "String"
                     }
                   },
                   "example-2":{
                     "summary": "PUT `/execution/anExecutionId/localVariables/aVarName`",
                     "value": {
                       "value": "<myobj><prop1>a</prop1><prop2>b</prop2></myobj>",
                       "type": "Object",
                       "valueInfo": {
                         "objectTypeName": "com.example.MyObject",
                         "serializationDataFormat": "application/xml"
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid, for example if the value could not be parsed
                to an Integer value or the passed variable type is not supported.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."
        last = true
    />

  }

}
</#macro>