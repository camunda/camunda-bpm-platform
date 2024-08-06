<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "TaskDto" >

        <@lib.property
        name = "attachment"
        type = "boolean"
        desc = "Specifies if an attachment exists for the task." />

        <@lib.property
        name = "comment"
        type = "boolean"
        last = true
        desc = "Specifies if an comment exists for the task." />

</@lib.dto>
</#macro>