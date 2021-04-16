<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTaskVariableBinary"
      tag = "Task Variable"
      summary = "Get Task Variable (Binary)"
      desc = "Retrieves a binary variable from the context of a given task. Applicable for byte array and file
              variables. The variable must be visible from the task. It is visible from the task if it is a local task
              variable or declared in a parent scope of the task. See documentation on
              [visiblity of variables](${docsUrl}/user-guide/process-engine/variables/)." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task to retrieve the variable for."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to retrieve."/>

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
            "example-1": {
              "summary": "GET /task/aTaskId/variables/aVarName/data",
              "value": "binary variable: Status 200. Content-Type: application/octet-stream"
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
            "example-1": {
              "summary": "GET /task/aTaskId/variables/aVarName/data",
              "value": "file variable: Status 200. Content-Type: text/plain; charset=UTF-8. Content-Disposition: attachment; filename=\"someFile.txt\""
            }
          }
        }
      }
    },

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Variable with given id exists but is not a binary variable.See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Variable with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
</#macro>