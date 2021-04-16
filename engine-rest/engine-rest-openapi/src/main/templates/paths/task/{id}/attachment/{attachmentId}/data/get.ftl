<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getAttachmentData"
      tag = "Task Attachment"
      summary = "Get (Binary)"
      desc = "Retrieves the binary content of a task attachment by task id and attachment id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task." />

    <@lib.parameter
        name = "attachmentId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the attachment to be retrieved." />

  ],

  "responses" : {

    "200": {
      "description": "Request successful.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "For files without any MIME type information, a byte stream is returned."
          }
        },
        "text/plain": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "Files with MIME type information are returned as the saved type. Additionally, for file
                            responses, the Content-Disposition header will be set."
          }
        }
      }
    },

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "The attachment content for the given task id and attachment id does not exist, or the history of the
                engine is disabled.

                See the [Introduction](/reference/rest/overview/#error-handling) for the error response format." />

  }
}

</#macro>