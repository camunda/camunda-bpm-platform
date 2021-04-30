<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/history-cleanup/get-cleanup-configuration/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "batchWindowStartTime"
        type = "string"
        format = "date-time"
        desc = "Start time of the current or next batch window."
    />
    
    <@lib.property
        name = "batchWindowEndTime"
        type = "string"
        format = "date-time"
        desc = "End time of the current or next batch window."
    />
    
    <@lib.property
        name = "enabled"
        type = "boolean"
        desc = "Indicates whether the engine node participates in history cleanup or
                not. The default is `true`. Participation can be disabled via
                [Process Engine Configuration](${docsUrl}/reference/deployment-descriptors/tags/process-engine/#history-cleanup-enabled).

                For more details, see
                [Cleanup Execution Participation per Node](${docsUrl}/user-guide/process-engine/history/#cleanup-execution-participation-per-node)."
        last = true
    />

</@lib.dto>
</#macro>