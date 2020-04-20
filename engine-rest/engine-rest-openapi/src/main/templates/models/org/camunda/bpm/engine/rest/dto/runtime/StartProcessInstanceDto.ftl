<@lib.dto
    extends = "ProcessInstanceDto" >

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The case instance id the process instance is to be initialized with." />

    <@lib.property
        name = "startInstructions"
        type = "array"
        dto = "ProcessInstanceModificationInstructionDto"
        desc = "**Optional**. A JSON array of instructions that specify which activities to start the process instance at.
                If this property is omitted, the process instance starts at its default blank start event." />

    <@lib.property
        name = "skipCustomListeners"
        type = "boolean"
        desc = "Skip execution listener invocation for activities that are started or ended as part of this request.
                **Note**: This option is currently only respected when start instructions are submitted
                via the `startInstructions` property." />

    <@lib.property
        name = "skipIoMappings"
        type = "boolean"
        desc = "Skip execution of
                [input/output variable mappings](${docsUrl}/user-guide/process-engine/variables/#input-output-variable-mapping)
                for activities that are started or ended as part of this request.
                **Note**: This option is currently only respected when start instructions are submitted
                via the `startInstructions` property." />
                

    <@lib.property
        name = "withVariablesInReturn"
        type = "boolean"
        last = true
        desc = "Indicates if the variables, which was used by the process instance during execution, should be returned.
                Default value: `false`" />

</@lib.dto>