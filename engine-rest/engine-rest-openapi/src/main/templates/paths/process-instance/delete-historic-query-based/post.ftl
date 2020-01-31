{
  "operationId": "deleteAsyncHistoricQueryBased",
  "description": "Deletes a set of process instances asynchronously (batch) based on a historic process instance query.",
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
                "historicProcessInstanceQuery": {
                  "$ref": "#/components/schemas/HistoricProcessInstanceQueryDto"
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
        Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor historicProcessInstanceQuery is present"/>

  }
}