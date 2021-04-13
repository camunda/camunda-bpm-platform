<#macro dto_macro docsUrl="">
<@lib.dto
    desc = 'Mandatory when `sortBy` is one of the following values: `processVariable`, `executionVariable`,
           `taskVariable`, `caseExecutionVariable` or `caseInstanceVariable`. Must be a JSON object with the properties
           `variable` and `type` where `variable` is a variable name and `type` is the name of a variable value type.' >

    <@lib.property
        name = "variable"
        type = "string"
        desc = "The name of the variable to sort by." />

    <@lib.property
        name = "type"
        type = "string"
        last = true
        desc = "The name of the type of the variable value." />

</@lib.dto>
</#macro>