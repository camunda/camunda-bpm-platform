<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/get-local-variables/index.html -->
{
  <@lib.endpointInfo
      id = "getLocalExecutionVariables"
      tag = "Execution"
      summary = "Get Local Execution Variables"
      desc = "Retrieves all variables of a given execution by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to retrieve the variables from."
      />

      <@lib.parameter
          name = "deserializeValues"
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
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "Request successful. Returns A JSON object of variables key-value pairs. Each key is a variable name and each value a VariableValueDto"
        examples = ['"example-1": {
                       "summary": "GET `/execution/anExecutionId/localVariables`",
                       "description": "GET `/execution/anExecutionId/localVariables`",
                       "value": {
                         "aVariableKey": {
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
                       }
                     },
                     "example-2": {
                       "summary": "GET `/execution/anExecutionId/localVariables?deserializeValues=false`",
                       "description": "GET `/execution/anExecutionId/localVariables?deserializeValues=false`",
                       "value": {
                         "aVariableKey": {
                           "value": "<myObj><prop1>a</prop1><prop2>b</prop2></myObj>",
                           "type": "Object",
                           "valueInfo": {
                             "objectTypeName": "com.example.MyObject",
                             "serializationDataFormat": "application/xml"
                           }
                         }
                       }
                     }']
    />
    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "Execution with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>