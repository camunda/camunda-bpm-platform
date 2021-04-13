<#macro dto_macro docsUrl="">
<@lib.dto extends = "LinkableDto" >

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the task attachment." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the task attachment." />

    <@lib.property
        name = "description"
        type = "string"
        desc = "The description of the task attachment." />

    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task to which the attachment belongs." />

    <@lib.property
        name = "type"
        type = "string"
        desc = "Indication of the type of content that this attachment refers to.
                Can be MIME type or any other indication." />

    <@lib.property
        name = "url"
        type = "string"
        desc = "The url to the remote content of the task attachment." />

    <@lib.property
        name = "createTime"
        type = "string"
        format = "date-time"
        desc = "The time the variable was inserted.
                [Default format](${docsUrl}/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the attachment should be removed by the History Cleanup job.
                [Default format](${docsUrl}/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        last = true
        desc = "The process instance id of the root process instance that initiated the process containing the task." />

</@lib.dto>
</#macro>