{
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