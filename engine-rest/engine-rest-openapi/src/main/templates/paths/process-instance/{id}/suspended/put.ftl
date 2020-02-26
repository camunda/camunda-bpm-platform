{
  "operationId" : "updateSuspensionStateById",
  "description": "Activates or suspends a given process instance by id.",
  "tags": [
    "Process instance"
  ],
  "parameters" : [ 
      <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        description = "The id of the process instance to activate or suspend."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceSuspensionStateDto"
      requestDescription = "**Only allowed property**: `suspended`, the others will be ignored" />

  "responses" : {

    <@lib.response
        code = "204"
        last = true
        description = "Request successful." />

      }
}