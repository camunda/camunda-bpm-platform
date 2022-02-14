<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/post-batch-set-removal-time/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto extends="AbstractSetRemovalTimeDto">

    <@lib.property
        name = "historicBatchQuery"
        type = "object"
        dto = "HistoricBatchQueryDto"
        desc = "Query for the historic batches to set the removal time for."
    />

    
    <@lib.property
        name = "historicBatchIds"
        type = "array"
        itemType = "string"
        desc = "The ids of the historic batches to set the removal time for."
        last = true
    />

</@lib.dto>
</#macro>