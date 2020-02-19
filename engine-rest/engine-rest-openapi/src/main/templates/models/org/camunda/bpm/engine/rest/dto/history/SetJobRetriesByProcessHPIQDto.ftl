{
  "allOf": [
    {
      "$ref": "#/components/schemas/SetJobRetriesByProcessDto"
    },
    {
      "type": "object",
      "properties": {
        "processInstanceQuery": {
          "$ref": "#/components/schemas/HistoricProcessInstanceQueryDto"
        }
      }
    }
  ]
}