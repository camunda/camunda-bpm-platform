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
          "$ref": "#/components/schemas/SetJobRetriesByProcessPIQDto"
        }
      }
    },
   "description": "Please note that if both processInstances and processInstanceQuery are provided, then the resulting execution will be performed on the union of these sets."
  },
  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        description = "Bad Request
        Returned if some of the query parameters are invalid, for example if neither processInstanceIds, nor processInstanceQuery is present. Or if the retry count is not specified."/>

  }
}