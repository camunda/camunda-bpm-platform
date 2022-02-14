<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/post-set-job-retries/index.html -->
<@lib.dto desc = "Defines the number of retries for a selection of jobs.
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
        desc = "A job query like the request body for the
                [Get Jobs (POST)](${docsUrl}/reference/rest/job/post-query/#request-body)
                method."
    />

    
    <@lib.property
        name = "retries"
        type = "integer"
        format = "int32"
        desc = "An integer representing the number of retries. Please note that the value cannot be
                negative or null."
        minimum = 0
        last = true
    />


</@lib.dto>

</#macro>