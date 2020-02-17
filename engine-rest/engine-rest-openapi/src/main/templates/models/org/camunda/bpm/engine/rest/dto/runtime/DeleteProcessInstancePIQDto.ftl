{
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
