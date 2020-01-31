{
  "operationId": "deleteProcessInstancesAsync",
  "description": "Deletes multiple process instances asynchronously (batch).",
  "tags": [
    "Process instance"
  ],
  "requestBody" : {
    "content" : {
      "application/json" : {
        "schema" : {
          "allOf": [
            {
              "$ref": "#/components/schemas/DeleteProcessInstancesDto"
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
    }
  },
  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
        Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor processInstanceQuery is present"/>

  }
}