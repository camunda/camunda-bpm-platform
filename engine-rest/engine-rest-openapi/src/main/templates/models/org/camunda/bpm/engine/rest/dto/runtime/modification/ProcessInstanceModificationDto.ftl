{
  "type" : "object",
  "properties" : {
    "skipCustomListeners" : {
      "type" : "boolean",
      "description": "Skip execution listener invocation for activities that are started or ended as part of this request."
    },
    "skipIoMappings" : {
      "type" : "boolean",
      "description": "Skip execution of input/output variable mappings (https://docs.camunda.org/manual/develop/user-guide/process-engine/variables/#input-output-variable-mapping) for activities that are started or ended as part of this request."
    },
    "instructions" : {
      "type" : "array",
      "items" : {
        "$ref" : "#/components/schemas/ProcessInstanceModificationInstructionDto"
      },
      "description": " JSON array of modification instructions. The instructions are executed in the order they are in."
    },
    "annotation" : {
      "type" : "string",
      "description": "An arbitrary text annotation set by a user for auditing reasons."
    }
  }
}