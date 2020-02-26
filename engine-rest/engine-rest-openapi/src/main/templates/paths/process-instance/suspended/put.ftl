{
  "operationId" : "updateSuspensionState",
  "description": "Activates or suspends process instances.",
  "tags": [
    "Process instance"
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceSuspensionStateDto" />

  "responses" : {

    <@lib.response
        code = "204"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        description = "Bad Request
Returned if some of the request parameters are invalid,
for example if the provided processDefinitionId or processDefinitionKey parameter is null."/>
  }
}