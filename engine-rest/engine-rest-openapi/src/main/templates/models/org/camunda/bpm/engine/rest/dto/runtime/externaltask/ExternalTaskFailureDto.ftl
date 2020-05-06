<@lib.dto>

  <@lib.property
    name = "workerId"
    type = "string"
    desc = "The id of the worker that reports the failure. Must match the id of the worker who has most recently
            locked the task." />

  <@lib.property
      name = "errorMessage"
      type = "string"
      desc = "An message indicating the reason of the failure." />

  <@lib.property
      name = "errorDetails"
      type = "string"
      desc = "A detailed error description." />

  <@lib.property
      name = "retries"
      type = "integer"
      format = "int32"
      nullable = false
      desc = "A number of how often the task should be retried. Must be >= 0. If this is 0, an incident is created and
              the task cannot be fetched anymore unless the retries are increased again. The incident's message is set
              to the `errorMessage` parameter." />

  <@lib.property
      name = "retryTimeout"
      type = "integer"
      format = "int64"
      nullable = false
      last = true
      desc = "A timeout in milliseconds before the external task becomes available again for fetching. Must be >= 0." />

</@lib.dto>
