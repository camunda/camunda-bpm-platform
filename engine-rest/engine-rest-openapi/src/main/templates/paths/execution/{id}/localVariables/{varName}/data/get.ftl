<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/get-local-variable-binary/index.html -->
{
  <@lib.endpointInfo
      id = "getLocalExecutionVariableBinary"
      tag = "Execution"
      summary = "Get Local Execution Variable (Binary)"
      desc = "Retrieves a binary variable from the context of a given execution by id. Does not
              traverse the parent execution hierarchy. Applicable for byte array and
              file variables."
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
          last = true
      />

  ],

  "responses": {

    "200": {
      "description": "Request successful.
        For binary variables or files without any MIME type information, a byte stream is returned.
        File variables with MIME type information are returned as the saved type.
        Additionally, for file variables the Content-Disposition header will be set.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "For binary variables or files without any MIME type information, a byte stream is returned."
          },
          "examples": {
            "example-1" :{
              "summary": "binary variable: Status 200",
              "value": "Content-Type: application/octet-stream"
            }
          }
        },
        "text/plain": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "File variables with MIME type information are returned as the saved type.
            Additionally, for file variables the Content-Disposition header will be set."
          },
          "examples": {
            "example-1" :{
              "summary": "file variable: Status 200",
              "value": "Content-Type: text/plain; charset=UTF-8. Content-Disposition: attachment; filename=\"someFile.txt\""
            }
          }
        }
      }
    },

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Variable instance with given id exists but is not a binary variable. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Variable instance with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>