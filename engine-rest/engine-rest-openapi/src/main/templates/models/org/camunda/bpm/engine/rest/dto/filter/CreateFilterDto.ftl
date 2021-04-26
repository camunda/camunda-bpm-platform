<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro dto_macro docsUrl="">

<@lib.dto >

    <@lib.property
        name = "resourceType"
        type = "string"
        desc = "The resource type of the filter."
    />
    
    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the filter."
    />
    
    <@lib.property
        name = "owner"
        type = "string"
        desc = "The user id of the owner of the filter."
    />
    
    <@lib.property
        name = "query"
        type = "object"
        desc = "The query of the filter as a JSON object."
    />
    
    <@lib.property
        name = "properties"
        type = "object"
        desc = "The properties of a filter as a JSON object."
        last = true
    />
    
</@lib.dto>
</#macro>