<#macro dto_macro docsUrl="">
<@lib.dto extends = "BatchDto">

    <@lib.property
        name = "remainingJobs"
        type = "integer"
        format = "int32"
        desc = "The number of remaining batch execution jobs. This does include failed batch execution jobs and
                batch execution jobs which still have to be created by the seed job." />

    <@lib.property
        name = "completedJobs"
        type = "integer"
        format = "int32"
        desc = "The number of completed batch execution jobs. This does include aborted/deleted batch execution jobs." />

    <@lib.property
        name = "failedJobs"
        type = "integer"
        format = "int32"
        last = true
        desc = "The number of failed batch execution jobs. This does not include aborted or deleted batch execution jobs." />

</@lib.dto>
</#macro>