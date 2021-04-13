<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/put-set-job-duedate/index.html -->
<@lib.dto desc = "">
    
    <@lib.property
        name = "duedate"
        type = "string"
        format = "date-time"
        desc = "The date to set when the job has the next execution."
    />

    
    <@lib.property
        name = "cascade"
        type = "boolean"
        desc = "A boolean value to indicate if modifications to the due date should cascade to
                subsequent jobs. (e.g. Modify the due date of a timer by +15
                minutes. This flag indicates if a +15 minutes should be applied to all
                subsequent timers.) This flag only affects timer jobs and only works if due date
                is not null. Default: `false`"
        last = true
    />


</@lib.dto>
</#macro>