<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/validate-migration-plan/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "instructionReports"
        type = "array"
        dto = "MigrationInstructionValidationReportDto"
        desc = "The list of instruction validation reports. If no validation
                errors are detected it is an empty list."
        last = true
    />

</@lib.dto>
</#macro>