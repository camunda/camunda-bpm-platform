<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/generate-migration/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "sourceActivityIds"
        type = "array"
        itemType = "string"
        desc = "The activity ids from the source process definition being mapped."
    />
    
    <@lib.property
        name = "targetActivityIds"
        type = "array"
        itemType = "string"
        desc = "The activity ids from the target process definition being mapped."
    />
    
    <@lib.property
        name = "updateEventTrigger"
        type = "boolean"
        desc = "Configuration flag whether event triggers defined are going to be updated during migration."
        last = true
    />

</@lib.dto>
</#macro>