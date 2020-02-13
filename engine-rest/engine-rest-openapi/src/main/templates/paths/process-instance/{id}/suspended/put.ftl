{
  "operationId" : "updateSuspensionState",
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
  "requestBody" : {
    "content" : {
      "application/json" : {
        "schema" : {
          "properties": {

            <@lib.property
                name = "suspended"
                type = "boolean"
                last = true
                description = "A Boolean value which indicates whether to activate or suspend a given process instance. When the value is set to true, the given process instance will be suspended and when the value is set to false, the given process instance will be activated." />

          }
        }
      }
    }
  },
  "responses" : {

    <@lib.response
        code = "204"
        last = true
        description = "Request successful." />

      }
}