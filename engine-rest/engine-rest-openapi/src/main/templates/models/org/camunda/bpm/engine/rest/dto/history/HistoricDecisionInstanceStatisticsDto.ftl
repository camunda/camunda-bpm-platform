<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-requirements-definition/get-statistics/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "decisionDefinitionKey"
        type = "string"
        desc = "A key of decision definition."
    />
    
    <@lib.property
        name = "evaluations"
        type = "integer"
        format = "int32"
        desc = "A number of evaluation for decision definition."
        last = true
    />

</@lib.dto>
</#macro>