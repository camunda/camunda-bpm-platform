<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/detail/get-detail-binary/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "historicDetailBinary"
      tag = "Historic Detail"
      summary = "Get Historic Detail (Binary)"
      desc = "Retrieves the content of a historic variable update by id. Applicable for byte
              array and file variables."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the historic variable update."
          last = true
      />

  ],

  "responses": {

    "200": {
      "description": "Request successful.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "For binary variables or files without any MIME type information, a byte stream is returned."
          },
          "examples": {
            "example-1": {
              "summary": "GET `/history/detail/someId/data`",
              "value": "binary variable: Status 200. Content-Type: application/octet-stream"
            }
          }
        },
        "*/*": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "File variables with MIME type information are returned as the saved type. Additionally,
                            for file variables the Content-Disposition header will be set."
          },
          "examples": {
            "example-1": {
              "summary": "GET `/history/detail/someId/data`",
              "value": "file variable: Status 200. Content-Type: text/plain; charset=UTF-8.
                        Content-Disposition: attachment; filename='someFile.txt'"
            }
          }
        }
      }
    },

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Detail with given id exists but is not a binary variable. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Detail with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>