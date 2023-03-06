<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/post-set-job-retries/index.html -->
<@lib.dto 
    extends = "JobRetriesDto"
    desc = "Defines the number of retries for a selection of jobs.
            Please note that if both jobIds and jobQuery are provided,
            then retries will be set on the union of these sets.">
    
    <@lib.property
        name = "jobIds"
        type = "array"
        itemType = "string"
        desc = "A list of job ids to set retries for."
    />

    <@lib.property
        name = "jobQuery"
        type = "ref"
        dto = "JobQueryDto"
        last = true
        desc = "A job query like the request body for the
                [Get Jobs (POST)](${docsUrl}/reference/rest/job/post-query/#request-body)
                method."
    />

</@lib.dto>

</#macro>