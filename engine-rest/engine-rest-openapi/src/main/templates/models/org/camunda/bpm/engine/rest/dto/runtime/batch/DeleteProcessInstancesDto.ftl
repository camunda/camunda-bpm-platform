{
  "type" : "object",
  "properties" : {
    <#-- NOTE: "processInstanceQuery" and "historicProcessInstanceQuery" are referenced in the request body
         Do not add them here -->
    "processInstanceIds" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      },
      "description": "A list process instance ids to delete."
    },
    "deleteReason" : {
      "type" : "string",
      "description": "A string with delete reason."
    },
    "skipCustomListeners" : {
      "type" : "boolean",
      "description" : "Skip execution listener invocation for activities that are started or ended as part of this request.",
      "default": true
    },
    "skipSubprocesses" : {
      "type" : "boolean",
      "description" : "Skip deletion of the subprocesses related to deleted processes as part of this request.",
      "default": false
    }
  }
}