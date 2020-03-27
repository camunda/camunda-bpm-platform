{

  <@lib.endpointInfo
      id = "getDeploymentResourceData"
      tag = "Deployment"
      desc = "Retrieves the binary content of a deployment resource for the given deployment by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the deployment." />

    <@lib.parameter
        name = "resourceId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the deployment resource." />

  ],

  "responses" : {

    "200": {
      "description": "Request successful. The media type of the response depends on the filename.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "description": "For files without any MIME type information, a byte stream is returned."
          }
        },
        "application/xml": {
          "schema": {
            "description": "Files with MIME type information are returned as the saved type. For example, a
                            `process.bpmn` resource will have the media type `application/xml`."
          }
        }
      }
    },

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Deployment Resource with given resource id or deployment id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
