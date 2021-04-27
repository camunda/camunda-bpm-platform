<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-definition/get-cleanable-decision-instance-report/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "decisionDefinitionId"
        type = "string"
        desc = "The id of the decision definition."
    />
    
    <@lib.property
        name = "decisionDefinitionKey"
        type = "string"
        desc = "The key of the decision definition."
    />
    
    <@lib.property
        name = "decisionDefinitionName"
        type = "string"
        desc = "The name of the decision definition."
    />
    
    <@lib.property
        name = "decisionDefinitionVersion"
        type = "integer"
        format = "int32"
        desc = "The version of the decision definition."
    />
    
    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        desc = "The history time to live of the decision definition."
    />
    
    <@lib.property
        name = "finishedDecisionInstanceCount"
        type = "integer"
        format = "int64"
        desc = "The count of the finished historic decision instances."
    />
    
    <@lib.property
        name = "cleanableDecisionInstanceCount"
        type = "integer"
        format = "int64"
        desc = "The count of the cleanable historic decision instances, referring to history time
                to live."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the decision definition."
        last = true
    />

</@lib.dto>
</#macro>