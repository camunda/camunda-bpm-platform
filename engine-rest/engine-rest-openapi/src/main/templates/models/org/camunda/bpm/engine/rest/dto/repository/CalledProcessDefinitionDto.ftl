<#-- Generated From File: camunda-docs-manual/public/reference/rest/process-definition/get-static-called-process-definitions/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto extends = "ProcessDefinitionDto">

    <@lib.property
        name = "calledFromActivityIds"
        type = "array"
        itemType = "string"
        desc = "Ids of the CallActivities which call this process."
    />
    
    <@lib.property
        name = "callingProcessDefinitionId"
        type = "string"
        desc = "The id of the calling process definition"
        last = true
    />

</@lib.dto>
</#macro>