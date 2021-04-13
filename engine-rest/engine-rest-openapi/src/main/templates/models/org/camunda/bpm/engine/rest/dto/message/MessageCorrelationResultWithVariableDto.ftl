<#macro dto_macro docsUrl="">
<@lib.dto
    desc="The `processInstance` property only has a value if the resultType is set to `ProcessDefinition`.
          The processInstance with the properties as described in the
          [get single instance](${docsUrl}/reference/rest/process-instance/get/) method.

          The `execution` property only has a value if the resultType is set to `Execution`.
          The execution with the properties as described in the
          [get single execution](${docsUrl}/reference/rest/execution/get/) method.">

    <@lib.property
        name = "resultType"
        type = "string"
        enumValues = [ '"Execution"', '"ProcessDefinition"' ]
        desc = "Indicates if the message was correlated to a message start event or an 
                intermediate message catching event. In the first case, the resultType is 
                `ProcessDefinition` and otherwise `Execution`."/>

    <@lib.property
        name = "processInstance"
        type = "ref"
        dto = "ProcessInstanceDto"/>

    <@lib.property
        name = "execution"
        type = "ref"
        dto = "ExecutionDto"/>

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        last = true
        desc = "This property is returned if the `variablesInResultEnabled` is set to `true`.
                Contains a list of the process variables. "/>

</@lib.dto>

</#macro>