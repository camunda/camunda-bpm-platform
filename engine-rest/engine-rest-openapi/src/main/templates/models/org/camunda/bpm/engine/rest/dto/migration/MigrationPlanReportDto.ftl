<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/validate-migration-plan/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "instructionReports"
        type = "array"
        dto = "MigrationInstructionValidationReportDto"
        desc = "The list of instruction validation reports. If no validation
                errors are detected it is an empty list."
    />

    <@lib.property
        name = "variableReports"
        type = "object"
        additionalProperties = true
        dto = "MigrationVariableValidationReportDto"
        desc = "A map of variable reports.
                Each key is a variable name and each value a JSON object consisting of the variable's type, value,
                value info object and a list of failures."
        last = true
    />

</@lib.dto>
</#macro>