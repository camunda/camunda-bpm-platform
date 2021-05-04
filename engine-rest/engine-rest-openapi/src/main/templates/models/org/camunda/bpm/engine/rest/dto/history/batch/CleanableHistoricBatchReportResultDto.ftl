<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get-cleanable-batch-report/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "batchType"
        type = "string"
        desc = "The type of the batch operation."
    />
    
    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        desc = "The history time to live of the batch operation."
    />
    
    <@lib.property
        name = "finishedBatchesCount"
        type = "integer"
        format = "int64"
        desc = "The count of the finished batch operations."
    />
    
    <@lib.property
        name = "cleanableBatchesCount"
        type = "integer"
        format = "int64"
        desc = "The count of the cleanable historic batch operations, referring to history time to
                live."
        last = true
    />

</@lib.dto>
</#macro>
