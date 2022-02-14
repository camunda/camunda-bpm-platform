<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getDecisionDefinitionDiagram"
      tag = "Decision Definition"
      summary = "Get Diagram"
      desc = "Retrieves the diagram of a decision definition." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process definition."/>
  ],

  "responses" : {

    "200": {
      "description": "Request successful. The image diagram of this process.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "defaults to `application/octet-stream` if the file suffix is unknown"
          }
        },
        "*/*": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "Files with MIME type information image/png, image/gif, ... "
          }
        }
      }
    },

    <@lib.response
        code = "204"
        desc = "The decision definition doesn't have an associated diagram." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Decision definition with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>