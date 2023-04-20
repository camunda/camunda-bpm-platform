<#macro dto_macro docsUrl="">
<@lib.dto 
    extends = "RetriesDto" >

    <@lib.property
        name = "dueDate"
        type = "string"
        format = "date-time"
        last = true
        desc = "The due date to set for the job. A due date indicates when this job is ready for execution.
                Jobs with due dates in the past will be scheduled for execution." />

</@lib.dto>

</#macro>