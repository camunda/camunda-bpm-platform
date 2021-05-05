<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-historic-activity-statistics-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the activity the results are aggregated for."
    />
    
    <@lib.property
        name = "instances"
        type = "integer"
        format = "int64"
        desc = "The total number of all running instances of the activity."
    />
    
    <@lib.property
        name = "canceled"
        type = "integer"
        format = "int64"
        desc = "The total number of all canceled instances of the activity. **Note:** Will be `0`
                (not `null`), if canceled activity instances were excluded."
    />
    
    <@lib.property
        name = "finished"
        type = "integer"
        format = "int64"
        desc = "The total number of all finished instances of the activity. **Note:** Will be `0`
                (not `null`), if finished activity instances were excluded."
    />
    
    <@lib.property
        name = "completeScope"
        type = "integer"
        format = "int64"
        desc = "The total number of all instances which completed a scope of the activity.
                **Note:** Will be `0` (not `null`), if activity instances which
                completed a scope were excluded."
    />
    
    <@lib.property
        name = "openIncidents"
        type = "integer"
        format = "int64"
        desc = "The total number of open incidents for the activity. **Note:** Will be `0` (not
                `null`), if `incidents` is set to `false`."
    />
    
    <@lib.property
        name = "resolvedIncidents"
        type = "integer"
        format = "int64"
        desc = "The total number of resolved incidents for the activity. **Note:** Will be `0` (not
                `null`), if `incidents` is set to `false`."
    />
    
    <@lib.property
        name = "deletedIncidents"
        type = "integer"
        format = "int64"
        desc = "The total number of deleted incidents for the activity. **Note:** Will be `0` (not
                `null`), if `incidents` is set to `false`."
        last = true
    />

</@lib.dto>
</#macro>
