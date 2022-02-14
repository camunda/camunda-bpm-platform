<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/put-local-variable-binary/index.html -->
{
  <@lib.endpointInfo
      id = "setLocalExecutionVariableBinary"
      tag = "Execution"
      summary = "Post Local Execution Variable (Binary)"
      desc = "Sets the serialized value for a binary variable or the binary value for a file
              variable in the context of a given execution by id."
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
      mediaType = "multipart/form-data"
      dto = "MultiFormVariableBinaryDto"
      examples = [
                '"example-1": {
                     "summary": "Post binary content of a byte array variable",
                     "description": "POST /execution/anExecutionId/localVariables/aVarName/data",
                     "value": "
                        ```
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y
                        Content-Disposition: form-data; name=\\"data\\"; filename=\\"unspecified\\"
                        Content-Type: application/octet-stream
                        Content-Transfer-Encoding: binary

                        <<Byte Stream ommitted>>
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y
                        Content-Disposition: form-data; name=\\"valueType\\"
                        Content-Type: text/plain; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        Bytes
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y--
                        ```
                      "
                }',
                '"example-2": {
                     "summary": "Post the JSON serialization of a Java Class **(deprecated)**",
                     "description": "POST /execution/anExecutionId/localVariables/aVarName/data",
                     "value": "
                        ```
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y
                        Content-Disposition: form-data; name=\\"data\\"
                        Content-Type: application/json; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        [\\"foo\\", \\"bar\\"]
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y
                        Content-Disposition: form-data; name=\\"type\\"
                        Content-Type: text/plain; charset=US-ASCII
                        Content-Transfer-Encoding: 8bit

                        java.util.ArrayList<java.lang.Object>
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y--
                        ```
                      "
                }',
                '"example-3": {
                     "summary": "Post a text file",
                     "description": "POST /execution/anExecutionId/localVariables/aVarName/data",
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
                        ---OSQH1f8lzs83iXFHphqfIuitaQfNKFY74Y--
                        ```
                      "
                }'


      ]
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid, for example if no filename is set. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>