<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/validate-migration-plan/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "instruction"
        type = "ref"
        dto = "MigrationInstructionDto"
        desc = "A migration instruction JSON object."
    />

    <@lib.property
        name = "failures"
        type = "array"
        itemType = "string"
        desc = "A list of instruction validation report messages."
        last = true
    />

</@lib.dto>
</#macro>