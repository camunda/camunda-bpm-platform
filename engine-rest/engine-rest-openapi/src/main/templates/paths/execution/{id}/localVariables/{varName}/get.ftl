<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/get-local-variable/index.html -->
{
  <@lib.endpointInfo
      id = "getLocalExecutionVariable"
      tag = "Execution"
      summary = "Get Local Execution Variable"
      desc = "Retrieves a variable from the context of a given execution by id. Does not traverse
              the parent execution hierarchy."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to retrieve the variable from."
      />

      <@lib.parameter
          name = "varName"
          location = "path"
          type = "string"
          required = true
          desc = "The name of the variable to get."
      />

      <@lib.parameter
          name = "deserializeValue"
          location = "query"
          type = "boolean"
          desc = "Determines whether serializable variable values (typically
                  variables that store custom Java objects) should be deserialized
                  on server side (default `true`).

                  If set to `true`, a serializable variable will be deserialized on
                  server side and transformed to JSON using
                  [Jackson's](https://github.com/FasterXML/jackson) POJO/bean
                  property introspection feature. Note that this requires the Java
                  classes of the variable value to be on the REST API's classpath.
                  If set to `false`, a serializable variable will be returned in its
                  serialized format. For example, a variable that is serialized as
                  XML will be returned as a JSON string containing XML.

                  **Note:** While `true` is the default value for reasons of
                  backward compatibility, we recommend setting this parameter to
                  `false` when developing web applications that are independent of
                  the Java process applications deployed to the engine."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "VariableValueDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/execution/anExecutionId/localVariables/aVarName`",
                       "description": "GET `/execution/anExecutionId/localVariables/aVarName`",
                       "value": {
                         "value": {
                           "prop1": "a",
                           "prop2": "b"
                         },
                         "type": "Object",
                         "valueInfo": {
                           "objectTypeName": "com.example.MyObject",
                           "serializationDataFormat": "application/xml"
                         }
                       }
                     },
                     "example-2": {
                       "summary": "GET `/execution/anExecutionId/localVariables/aVarName?deserializeValue=false`",
                       "description": "GET `/execution/anExecutionId/localVariables/aVarName?deserializeValue=false`",
                       "value": {
                         "value": "<myobj><prop1>a</prop1><prop2>b</prop2></myobj>",
                         "type": "Object",
                         "valueInfo": {
                           "objectTypeName": "com.example.MyObject",
                           "serializationDataFormat": "application/xml"
                         }
                       }
                     }
                     ']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Variable with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>