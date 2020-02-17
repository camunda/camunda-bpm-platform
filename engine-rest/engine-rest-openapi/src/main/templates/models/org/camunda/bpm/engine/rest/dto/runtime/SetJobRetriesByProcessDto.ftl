{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        description = "A list of process instance ids to fetch jobs, for which retries will be set." />

    <@lib.property
        name = "retries"
        type = "integer"
        format = "int32"
        minimum = 0
        last = true
        description = "An integer representing the number of retries. Please note that the value cannot be negative or null." />

  }
}