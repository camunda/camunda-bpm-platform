<#macro dto_macro docsUrl="">
<@lib.dto>
    <#-- Note: If you are here from Job endpoints, see JobConditionQueryParameterDto which has a reduced set of operators -->
    <@lib.property
        name = "operator"
        type = "string"
        enumValues = ['"eq"', '"neq"', '"gt"', '"gteq"', '"lt"', '"lteq"', '"like"']
        desc = "Comparison operator to be used"/>

    <@lib.property
        name = "value"
        type = "object"
        last = true
        desc = "The variable value, could be of type boolean, string or number"/>

</@lib.dto>

</#macro>