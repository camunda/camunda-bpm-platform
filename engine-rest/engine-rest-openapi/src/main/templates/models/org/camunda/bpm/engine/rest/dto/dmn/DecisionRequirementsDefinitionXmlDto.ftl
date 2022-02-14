<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-xml/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the decision requirements definition."
    />
    
    <@lib.property
        name = "dmnXml"
        type = "string"
        desc = "An escaped XML string containing the XML that this decision requirements definition
                was deployed with. Carriage returns, line feeds and quotation marks
                are escaped."
        last = true
    />

</@lib.dto>
</#macro>