{
  "operationId" : "updateSuspensionStateAsync",
  "description": "Activates or suspends process instances asynchronously with a list of process instance ids, a process instance query, and/or a historical process instance query.",
  "tags": [
    "Process instance"
  ],
  "requestBody" : {
    "content" : {
      "application/json" : {
        "schema" : {
          "$ref": "#/components/schemas/ProcessInstanceSuspensionStateQueriesDto"
        }
      }
    }
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
        Returned if some of the request parameters are invalid, for example if the provided processDefinitionId or processDefinitionKey parameter is null."/>
  }
}