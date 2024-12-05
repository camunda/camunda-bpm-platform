<#macro dto_macro docsUrl="">
    <@lib.dto>
        <@lib.property
            name = "decisionInstanceId"
            type = "string"
            desc = "The form key." />

        <@lib.property
            name = "result"
            type = "array"
            last = true
            dto = "DecisionResultDto" />
    </@lib.dto>
</#macro>
