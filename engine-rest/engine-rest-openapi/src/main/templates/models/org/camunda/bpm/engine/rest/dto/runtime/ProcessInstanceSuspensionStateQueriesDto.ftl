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