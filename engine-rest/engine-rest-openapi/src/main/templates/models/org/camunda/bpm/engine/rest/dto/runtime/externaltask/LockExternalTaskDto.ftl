<@lib.dto extends = "HandleExternalTaskDto" >

  <@lib.property
      name = "lockDuration"
      type = "integer"
      format = "int64"
      nullable = false
      last = true
      desc = "The duration to lock the external task for in milliseconds." />

</@lib.dto>
