<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
    name = "workerId"
    type = "string"
    nullable = false
    last = true
    desc = "**Mandatory.** The ID of the worker who is performing the operation on the external task.
            If the task is already locked, must match the id of the worker who has most recently
            locked the task." />

</@lib.dto>
</#macro>