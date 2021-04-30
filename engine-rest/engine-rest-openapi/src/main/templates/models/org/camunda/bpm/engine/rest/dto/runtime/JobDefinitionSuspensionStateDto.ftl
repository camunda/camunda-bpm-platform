<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/put-activate-suspend-by-id/index.html -->
<#macro dto_macro docsUrl="">

<#-- This DTO is also used in JobDefinitionsSuspensionStateDto that caters updating multiple definitions at once and 
     provides additional parameters for that which don't make sense here. Please also consider this other DTO when
     making changes here -->
<@lib.dto extends="SuspensionStateDto">
    
    <@lib.property
        name = "includeJobs"
        type = "boolean"
        desc = "A `Boolean` value which indicates whether to activate or suspend also all jobs of
                the referenced job definitions. When the value is set to `true`, all jobs
                of the provided job definitions will be activated or suspended and
                when the value is set to `false`, the suspension state of all jobs
                of the provided job definitions will not be updated."
    />

    
    <@lib.property
        name = "executionDate"
        type = "string"
        desc = "The date on which the referenced job definitions will be activated or suspended. If null,
                the suspension state of the given job definitions is updated
                immediately. By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the format `yyyy-MM-
                dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
        last = true
    />


</@lib.dto>
</#macro>
