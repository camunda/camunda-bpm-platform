{
  "type" : "object",
  "properties" : {
    <#-- NOTE: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         "processInstanceQuery" and "historicProcessInstanceQuery" are referenced in the request body
         Do not add them here
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! -->

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        description = "A list of process instance ids to fetch jobs, for which retries will be set." />

    <@lib.property
        name = "retries"
        type = "integer"
        format = "int32"
        hasMinimum = true
        minimum = 0
        last = true
        description = "An integer representing the number of retries. Please note that the value cannot be negative or null." />

  }
}