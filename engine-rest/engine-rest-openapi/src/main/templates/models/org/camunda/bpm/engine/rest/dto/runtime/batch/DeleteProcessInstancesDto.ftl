{
  "type" : "object",
  "properties" : {
    "processInstanceIds" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      },
      "description": "A list process instance ids to delete."
    },
    "processInstanceQuery" : {
      "$ref" : "#/components/schemas/ProcessInstanceQueryDto"
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
    <#-- 
    "historicProcessInstanceQuery" : {
      "$ref" : "#/components/schemas/HistoricProcessInstanceQueryDto"
    },
    -->
    "skipSubprocesses" : {
      "type" : "boolean",
      "description" : "Skip deletion of the subprocesses related to deleted processes as part of this request.",
      "default": false
    }
  }
}