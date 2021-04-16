<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-create-incident/index.html -->
<@lib.dto desc = "">
    
    <@lib.property
        name = "incidentType"
        type = "string"
        desc = "A type of the new incident."
    />
    
    <@lib.property
        name = "configuration"
        type = "string"
        desc = "A configuration for the new incident."
    />
    
    <@lib.property
        name = "message"
        type = "string"
        desc = "A message for the new incident."
        last = true
    />


</@lib.dto>
</#macro>