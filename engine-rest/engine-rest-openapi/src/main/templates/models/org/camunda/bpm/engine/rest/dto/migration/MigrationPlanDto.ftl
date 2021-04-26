<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/generate-migration/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "sourceProcessDefinitionId"
        type = "string"
        desc = "The id of the source process definition for the migration."
    />
    
    <@lib.property
        name = "targetProcessDefinitionId"
        type = "string"
        desc = "The id of the target process definition for the migration."
    />
    
    <@lib.property
        name = "instructions"
        type = "array"
        dto = "MigrationInstructionDto"
        desc = "
                A list of migration instructions which map equal activities. Each
                      migration instruction is a JSON object with the following
                properties:
                "
        last = true
    />

</@lib.dto>
</#macro>