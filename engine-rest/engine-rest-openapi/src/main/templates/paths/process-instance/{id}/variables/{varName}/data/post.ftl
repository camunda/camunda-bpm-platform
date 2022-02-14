<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setProcessInstanceVariableBinary"
      tag = "Process Instance"
      summary = "Update Process Variable (Binary)"
      desc = "Sets the serialized value for a binary variable or the binary value for a file variable." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process instance to retrieve the variable for."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to retrieve."/>

  ],

  <@lib.requestBody
      mediaType = "multipart/form-data"
      dto = "MultiFormVariableBinaryDto"
      requestDesc = "For binary variables a multipart form submit with the following parts:"
      examples = [ '"example-1": {
                      "summary": "POST `/process-instance/aProcessInstanceId/variables/aVarName/data` (1)",
                      "description": "Post binary content of a byte array variable.",
                      "value": "
                        ```
                        ------------------------------354ddb6baeff
                        Content-Disposition: form-data; name=\\"data\\"; filename=\\"image.png\\"
                        Content-Type: application/octet-stream
                        Content-Transfer-Encoding: binary

                        <<Byte Stream ommitted>>
                        ------------------------------354ddb6baeff
                        Content-Disposition: form-data; name=\\"valueType\\"
                        Content-Type: text/plain; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        Bytes
                        ------------------------------1e838f8f632a--
                        ```
                      "
                    }',
                   '"example-2": {
                      "summary": "POST `/process-instance/aProcessInstanceId/variables/aVarName/data` (2)",
                      "description": "Post the JSON serialization of a Java Class (**deprecated**).",
                      "value": "
                        ```
                        ------------------------------1e838f8f632a
                        Content-Disposition: form-data; name=\\"type\\"
                        Content-Type: text/plain; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        java.util.ArrayList<java.lang.Object>
                        ------------------------------1e838f8f632a
                        Content-Disposition: form-data; name=\\"data\\"
                        Content-Type: application/json; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        [\\"foo\\",\\"bar\\"]
                        ------------------------------1e838f8f632a--
                        ```
                      "
                    }',
                   '"example-3": {
                      "summary": "POST `/process-instance/aProcessInstanceId/variables/aVarName/data` (3)",
                      "description": "Post a text file.",
                      "value": "
                        ```
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y
                        Content-Disposition: form-data; name=\\"data\\"; filename=\\"myFile.txt\\"
                        Content-Type: text/plain; charset=US-ASCII
                        Content-Transfer-Encoding: binary

                        <<Byte Stream ommitted>>
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y
                        Content-Disposition: form-data; name=\\"valueType\\"
                        Content-Type: text/plain; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        File
                        ------------------------------1e838f8f632a--
                        ```
                      "
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
                The variable value or type is invalid, for example if no filename is set."/>

  }
}
</#macro>