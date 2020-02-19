{
  "operationId": "deleteProcessInstancesAsync",
  "description": "Deletes multiple process instances asynchronously (batch).",
  "tags": [
    "Process instance"
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteProcessInstancePIQDto" />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        description = "Bad Request
        Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor processInstanceQuery is present"/>

  }
}