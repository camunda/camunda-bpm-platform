<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "variables"
        type = "object"
        dto = "VariableValueDto"
        additionalProperties = true
        desc = "A JSON object containing variable key-value pairs." />

    <@lib.property
        name = "withVariablesInReturn"
        type = "boolean"
        defaultValue = "false"
        last =  true
        desc = "Indicates whether the response should contain the process variables or not. The
                default is `false` with a response code of `204`. If set to `true` the response
                contains the process variables and has a response code of `200`. If the task is not
                associated with a process instance (e.g. if it's part of a case instance) no
                variables will be returned." />

</@lib.dto>

</#macro>