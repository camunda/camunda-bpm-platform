{
  "type" : "object",
  "properties" : {
    <#-- NOTE: "processInstanceQuery" and "historicProcessInstanceQuery" are referenced in the request body
         Do not add them here -->
    "processInstances" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      },
      "description": "A list of process instance ids to fetch jobs, for which retries will be set."
    },
    "retries" : {
      "type" : "integer",
      "format" : "int32",
      "description": "An integer representing the number of retries. Please note that the value cannot be negative or null."
    }
  }
}