<#--
GIT Issue: https://github.com/camunda/camunda-bpm-platform/issues/2749
-->
<#macro dto_macro docsUrl="">
    <@lib.dto>
        <@lib.property
            name = "decisionInstanceId"
            type = "string"
            desc = "The form key." />

        <@lib.property
            name = "result"
            type = "array"
            addProperty = "\"additionalProperties\": true"
            last = true
            dto = "VariableValueDto" />
    </@lib.dto>
</#macro>
