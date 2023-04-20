<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/get-variable-instance-binary/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricVariableInstanceBinary"
      tag = "Historic Variable Instance"
      summary = "Get Variable Instance (Binary)"
      desc = "Retrieves the content of a historic variable by id. Applicable for variables that
              are serialized as binary data."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the variable instance."
          last = true
      />

  ],

  "responses": {
  
    <@lib.multiTypeResponse
        code = "200"
        desc = "Request successful."
        types = [
          {
            "binary": true,
            "mediaType": "application/octet-stream",
            "examples": ['"example-1": {
                       "summary": "GET `/history/variable-instance/someId/data`",
                       "description": "For binary variables or files without any MIME type information, a byte stream is returned.",
                       "value": ""
                     }']
          },
          {
            "binary": true,
            "mediaType": "*/*",
            "examples": ['"example-1": {
                       "summary": "GET `/history/variable-instance/someId/data`",
                       "description": "File variables with MIME type information are returned as the saved type. Additionally,
                            for file variables the Content-Disposition header will be set.",
                       "value": ""
                     }']
          }
        ]
        
        
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Variable with given id exists but is not a binary variable. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
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