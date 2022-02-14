<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "ExceptionDto" >

    <@lib.property
        name = "details"
        type = "object"
        additionalProperties = true
        dto = "ResourceReportDto"
        last = true
        desc = "A JSON Object containing list of errors and warnings occurred during deployment." />

</@lib.dto>
</#macro>