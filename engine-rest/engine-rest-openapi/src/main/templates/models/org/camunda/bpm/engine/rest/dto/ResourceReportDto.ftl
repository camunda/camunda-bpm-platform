<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "errors"
        type = "array"
        dto = "ProblemDto"
        desc = "A list of errors occurred during parsing."/>

    <@lib.property
        name = "warnings"
        type = "array"
        dto = "ProblemDto"
        last = true
        desc = "A list of warnings occurred during parsing."/>

</@lib.dto>
</#macro>