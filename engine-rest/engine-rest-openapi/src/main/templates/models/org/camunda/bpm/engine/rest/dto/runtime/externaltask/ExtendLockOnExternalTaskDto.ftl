<@lib.dto>

  <@lib.property
      name = "workerId"
      type = "string"
      desc = "The ID of a worker who is locking the external task." />

  <@lib.property
      name = "newDuration"
      type = "integer"
      format = "int64"
      nullable = false
      last = true
      desc = "An amount of time (in milliseconds). This is the new lock duration starting from the current moment." />

</@lib.dto>
