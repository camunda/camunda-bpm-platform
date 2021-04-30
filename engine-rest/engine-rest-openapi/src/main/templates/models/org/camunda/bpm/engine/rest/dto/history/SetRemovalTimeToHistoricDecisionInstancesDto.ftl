<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-instance/post-decision-instance-set-removal-time/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto extends = "AbstractSetRemovalTimeDto">
    
    <@lib.property
        name = "hierarchical"
        type = "boolean"
        desc = "Sets the removal time to all historic decision instances in the hierarchy.
                Value may only be `true`, as `false` is the default behavior."
    />
    
    <@lib.property
        name = "historicDecisionInstanceQuery"
        type = "ref"
        dto  = "HistoricDecisionInstanceQueryDto"
        desc = "Query for the historic decision instances to set the removal time for."
    />
    
    <@lib.property
        name = "historicDecisionInstanceIds"
        type = "array"
        itemType = "string"
        desc = "The ids of the historic decision instances to set the removal time for."
        last = true
    />


</@lib.dto>
</#macro>