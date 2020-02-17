{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        description = "A list process instance ids to delete." />

    <@lib.property
        name = "deleteReason"
        type = "string"
        description = "A string with delete reason." />

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        description = "Skip execution listener invocation for activities that are started or ended as part of this request." />

    <@lib.property
        name = "skipSubprocesses"
        type = "boolean"
        last = true
        description = "Skip deletion of the subprocesses related to deleted processes as part of this request." />

  }
}