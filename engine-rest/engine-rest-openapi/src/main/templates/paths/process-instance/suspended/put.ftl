{
  "operationId" : "updateSuspensionStateByProcessDefinition",
  "description": "Activates or suspends process instances with the given process definition.",
  "tags": [
    "Process instance"
  ],
  "requestBody" : {
    "content" : {
      "application/json" : {
        "schema" : {
          "oneOf": [
            {
              "properties": {

                <@lib.property
                    name = "suspended"
                    type = "boolean"
                    description = "A Boolean value which indicates whether to activate or suspend a given process instance. When the value is set to true, the given process instance will be suspended and when the value is set to false, the given process instance will be activated." />

                <@lib.property
                    name = "processDefinitionId"
                    type = "string"
                    last = true
                    description = "The process definition id of the process instances to activate or suspend." />

              }
            },
            {
              "properties": {

                <@lib.property
                    name = "suspended"
                    type = "boolean"
                    description = "A Boolean value which indicates whether to activate or suspend a given process instance. When the value is set to true, the given process instance will be suspended and when the value is set to false, the given process instance will be activated." />

                <@lib.property
                    name = "processDefinitionKey"
                    type = "string"
                    description = "The process definition key of the process instances to activate or suspend." />

                <@lib.property
                    name = "processDefinitionTenantId"
                    type = "string"
                    description = "Only activate or suspend process instances of a process definition which belongs to a tenant with the given id." />

                <@lib.property
                    name = "processDefinitionWithoutTenantId"
                    type = "boolean"
                    defaultValue = 'false'
                    last = true
                    description = "Only activate or suspend process instances of a process definition which belongs to no tenant. Value may only be true, as false is the default behavior." />

              }
            },
            {
              "properties": {

                <@lib.property
                    name = "suspended"
                    type = "boolean"
                    description = "A Boolean value which indicates whether to activate or suspend a given process instance. When the value is set to true, the given process instance will be suspended and when the value is set to false, the given process instance will be activated." />

                <@lib.property
                    name = "processInstanceIds"
                    type = "array"
                    itemType = "string"
                    description = "A list of process instance ids which defines a group of process instances which will be activated or suspended by the operation." />

                "processInstanceQuery": {
                  "$ref": "#/components/schemas/ProcessInstanceQueryDto"
                },
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
  "responses" : {

    <@lib.response
        code = "204"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        description = "Bad Request
        Returned if some of the request parameters are invalid, for example if the provided processDefinitionId or processDefinitionKey parameter is null."/>
  }
}