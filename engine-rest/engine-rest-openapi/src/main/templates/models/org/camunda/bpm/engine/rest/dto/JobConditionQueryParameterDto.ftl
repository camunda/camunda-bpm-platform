<#macro dto_macro docsUrl="">
<@lib.dto>
    <#-- Note: this is an extra Dto for the POST Job endpoints as it allows fewer comparison operators than the ConditionQueryParameterDto -->
    <@lib.property
        name = "operator"
        type = "string"
        enumValues = ['"gt"', '"lt"']
        desc = "Comparison operator to be used."/>

    <@lib.property
        name = "value"
        type = "string"
        format = "date-time"
        last = true
        desc = "Date value to compare with."/>

</@lib.dto>

</#macro>