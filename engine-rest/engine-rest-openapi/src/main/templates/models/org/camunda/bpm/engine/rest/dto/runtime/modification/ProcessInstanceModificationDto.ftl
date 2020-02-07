{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        description = "Skip execution listener invocation for activities that are started or ended as part of this request." />

    <@lib.property
        name = "skipIoMappings"
        type = "boolean"
        description = "Skip execution of input/output variable mappings (https://docs.camunda.org/manual/${docsVersion}/user-guide/process-engine/variables/#input-output-variable-mapping) for activities that are started or ended as part of this request." />

    <@lib.property
        name = "instructions"
        type = "array"
        dto = "ProcessInstanceModificationInstructionDto"
        description = "JSON array of modification instructions. The instructions are executed in the order they are in." />

    <@lib.property
        name = "annotation"
        type = "string"
        last = true
        description = "An arbitrary text annotation set by a user for auditing reasons." />

  }
}