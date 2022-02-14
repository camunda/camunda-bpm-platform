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
    />

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A map of variables which will be set into the process instances' scope.
                Each key is a variable name and each value a JSON variable value object."
        last = true
    />

</@lib.dto>
</#macro>