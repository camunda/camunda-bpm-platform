<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-set-job-priority/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto>
    
    <@lib.property
        name = "priority"
        type = "integer"
        format = "int64"
        desc = "The new execution priority number for jobs of the given definition. The
                definition's priority can be reset by using the value `null`. In
                that case, the job definition's priority no longer applies but a new
                job's priority is determined as specified in the process model."
    />

    
    <@lib.property
        name = "includeJobs"
        type = "boolean"
        desc = "A boolean value indicating whether existing jobs of the given definition should
                receive the priority as well. Default value is `false`. Can only be
                `true` when the __priority__ parameter is not `null`."
        last = true
    />


</@lib.dto>
</#macro>