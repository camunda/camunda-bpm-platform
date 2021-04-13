<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setProcessInstanceVariable"
      tag = "Process Instance"
      summary = "Update Process Variable"
      desc = "Sets a variable of a given process instance by id." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process instance to set the variable for." />

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to set." />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "VariableValueDto"
      examples = [ '"example-1": {
                       "summary": "PUT /process-instance/aProcessInstanceId/variables/aVarName",
                       "description": "Status 204. No content.",
                       "value": {
                         "value": "someValue",
                         "type": "String"
                       }
                     }',
                    '"example-2": {
                       "summary": "PUT /process-instance/aProcessInstanceId/variables/aVarName",
                       "description": "Status 204. No content.",
                       "value": {
                         "value": "ab",
                         "type": "Object",
                         "valueInfo": {
                           "objectTypeName": "com.example.MyObject",
                           "serializationDataFormat": "application/xml"
                         }
                       }
                     }'
      ] />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                The variable value or type is invalid, for example if the value could not be parsed to an Integer value or
                the passed variable type is not supported."/>
  }
}
</#macro>