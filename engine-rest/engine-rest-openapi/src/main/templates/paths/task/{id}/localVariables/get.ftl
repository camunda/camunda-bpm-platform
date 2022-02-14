<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTaskLocalVariables"
      tag = "Task Local Variable"
      summary = "Get Local Task Variables"
      desc = "Retrieves all variables of a given task by id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task to retrieve the variables from."/>

    <@lib.parameter
        name = "deserializeValues"
        location = "query"
        type = "boolean"
        defaultValue = "true"
        last = true
        desc = "Determines whether serializable variable values (typically variables that store custom Java objects)
                should be deserialized on the server side (default `true`).

                If set to `true`, a serializable variable will be deserialized on server side and transformed to JSON
                using [Jackson's](https://github.com/FasterXML/jackson) POJO/bean property introspection feature.
                Note that this requires the Java classes of the variable value to be on the REST API's classpath.

                If set to `false`, a serializable variable will be returned in its serialized format.
                For example, a variable that is serialized as XML will be returned as a JSON string containing XML.

                **Note:** While `true` is the default value for reasons of backward compatibility, we recommend setting this
                parameter to `false` when developing web applications that are independent of the Java process
                applications deployed to the engine." />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "VariableValueDto"
        additionalProperties = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/task/aTaskId/variables`",
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
                     }',
                    '"example-2": {
                       "summary": "GET `/task/aTaskId/variables?deserializeValue=false`",
                       "value": {
                         "aVariableKey": {
                           "value": "ab",
                           "type": "Object",
                           "valueInfo": {
                             "objectTypeName": "com.example.MyObject",
                             "serializationDataFormat": "application/xml"
                           }
                         }
                       }
                     }'
        ] />


    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Task id is `null` or does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>