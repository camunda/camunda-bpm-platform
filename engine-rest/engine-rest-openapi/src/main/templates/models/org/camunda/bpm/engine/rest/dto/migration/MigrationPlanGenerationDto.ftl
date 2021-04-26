<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/generate-migration/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto>
    
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
        name = "updateEventTriggers"
        type = "boolean"
        desc = "A boolean flag indicating whether instructions between events should be configured
                to update the event triggers."
        last = true
    />


</@lib.dto>
</#macro>