{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        desc = "A list process instance ids to delete." />

    <@lib.property
        name = "deleteReason"
        type = "string"
        desc = "A string with delete reason." />

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        desc = "Skip execution listener invocation for activities that are started or ended as part of this request." />

    <@lib.property
        name = "skipSubprocesses"
        type = "boolean"
        desc = "Skip deletion of the subprocesses related to deleted processes as part of this request." />

     "processInstanceQuery": {
       "$ref": "#/components/schemas/ProcessInstanceQueryDto"
     },
     "historicProcessInstanceQuery": {
       "$ref": "#/components/schemas/HistoricProcessInstanceQueryDto"
     }
  }
}