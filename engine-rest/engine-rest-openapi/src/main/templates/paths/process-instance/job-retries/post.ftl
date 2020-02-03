{
  "operationId" : "setRetriesByProcess",
  "description": "Create a batch to set retries of jobs associated with given processes asynchronously.",
  "tags": [
    "Process instance"
  ],
  "requestBody" : {
    "content" : {
      "application/json" : {
        "schema" : {
          "allOf": [
            {
              "$ref": "#/components/schemas/SetJobRetriesByProcessDto"
            },
            {
              "type": "object",
              "properties": {
                "processInstanceQuery": {
                  "$ref": "#/components/schemas/ProcessInstanceQueryDto"
                }
              }
            }
          ]
        }
      }
    },
   "description": "Please note that if both processInstances and processInstanceQuery are provided, then the resulting execution will be performed on the union of these sets."
  },
  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
        Returned if some of the query parameters are invalid, for example if neither processInstanceIds, nor processInstanceQuery is present. Or if the retry count is not specified."/>

  }
}